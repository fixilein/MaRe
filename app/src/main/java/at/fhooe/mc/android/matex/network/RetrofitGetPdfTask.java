package at.fhooe.mc.android.matex.network;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import at.fhooe.mc.android.matex.R;
import at.fhooe.mc.android.matex.document.Document;
import at.fhooe.mc.android.matex.ui.pdfpreview.PDFPreviewFragment;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;

public class RetrofitGetPdfTask extends AsyncTask<Void, Void, Void> {


    private final PDFPreviewFragment mPdfFragment;
    private final Document mDocument;

    private MatexBackend mService;

    private WeakReference<Context> mContext;

    public RetrofitGetPdfTask(Context context, PDFPreviewFragment _pdfFragment, Document _document) {
        mPdfFragment = _pdfFragment;
        mDocument = _document;
        mContext = new WeakReference<>(context);
    }


    @Override
    protected Void doInBackground(Void... voids) {
        Context context = mContext.get();
        if (context == null) {
            mPdfFragment.setError(mPdfFragment.getString(R.string.error_context));
            return null;
        }

        mDocument.getPDFFile(context).delete();

        try {
            createRetrofitService();

            String id = getId();

            uploadMdFile(id);

            for (File f : mDocument.getImageDir(context).listFiles()) {
                uploadImage(id, f);
            }

            getPdf(id);

        } catch (IOException e) {
            mPdfFragment.setError(context.getString(R.string.error_connection));
            e.printStackTrace();
        }

        return null;
    }

    private void createRetrofitService() {

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MatexBackend.SERVER)
                .addConverterFactory(new ToStringConverterFactory())
                .client(okHttpClient)
                .build();
        mService = retrofit.create(MatexBackend.class);
    }


    private String getId() throws java.io.IOException {
        return mService.getId().execute().body();
    }

    private String getLog(String id) throws IOException {
        ResponseBody body = mService.getLog(id).execute().body();
        if (body != null) return body.string();
        else return "";
    }

    private void uploadMdFile(String id) throws IOException {
        File mdFile = mDocument.getFile();
        RequestBody filePart = RequestBody.create(MediaType.get("text/markdown"), mdFile);
        MultipartBody.Part file = MultipartBody.Part.createFormData("upload", mdFile.getName(), filePart);

        mService.uploadMdFile(id, file).execute();
    }

    private void uploadImage(String id, File photo) throws IOException {
        RequestBody filePart = RequestBody.create(MediaType.get("image/png"), photo);
        MultipartBody.Part file = MultipartBody.Part.createFormData("image", photo.getName(), filePart);

        mService.uploadImage(id, file).execute();
    }

    private void getPdf(String id) throws IOException {
        Context context = mContext.get();
        if (context == null) {
            mPdfFragment.setError(mPdfFragment.getString(R.string.error_context));
            return;
        }
        File pdf = mDocument.getPDFFile(context);
        ResponseBody body = mService.downloadPdf(id).execute().body();
        if (body != null)
            writeResponseBodyToDisk(body, pdf);
        else { // error
            if (mPdfFragment.getContext() != null)
                mPdfFragment.setError(mPdfFragment.getString(R.string.error_generate_pdf)
                        + "\n\n" + getLog(id));
        }

    }


    @Override
    protected void onPostExecute(Void aVoid) {
        Context context = mContext.get();
        if (context == null) {
            mPdfFragment.setError(mPdfFragment.getString(R.string.error_context));
            return;
        }
        File pdf = mDocument.getPDFFile(context);
        mPdfFragment.loadPdf(pdf);
        super.onPostExecute(aVoid);
    }

    /**
     * Boilder Plate Java Crap
     */
    private boolean writeResponseBodyToDisk(ResponseBody body, File pdfFile) {
        try {
            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(pdfFile.getAbsolutePath());

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    // Log.d("Matex", "file download: " + fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }


}