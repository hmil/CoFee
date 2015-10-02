package fr.hmil.cofee;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import fr.hmil.cofee.models.Count;

public final class ServerAPI {
    public static final String BASE_URL = "http://128.179.176.206:3000";

    private static ServerAPI ourInstance = null;


    private CookieManager cookieManager;

    private SharedPreferences prefs;

    public static ServerAPI getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new ServerAPI(context);
        }
        return ourInstance;
    }

    private ServerAPI(Context context) {
        cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        prefs = context.getSharedPreferences("serverAPI", Context.MODE_PRIVATE);
        try {
            cookieManager.getCookieStore().add(new URI(BASE_URL), new HttpCookie("token",prefs.getString("token", "")));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    // Only checks that we have a token. Does not check for token validity
    public boolean isLoggedIn() {
        try {
            for (HttpCookie c : cookieManager.getCookieStore().get(new URI(BASE_URL))) {
                if (c.getName().equals("token")) {
                    return true;
                }
            }
            return false;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void logout() {
        cookieManager.getCookieStore().removeAll();
    }

    public static class APIException extends Exception {
        private String code;
        public APIException(String code, String message) {
            super(message);
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    public static class RegisterTaskParams {
        public final String username;
        public final String password;

        public RegisterTaskParams(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    public static class RegisterTaskResult {
        public final boolean success;
        public final Error error;

        public RegisterTaskResult(boolean success, Error error) {
            this.success = success;
            this.error = error;
        }
    }

    public static class RegisterTask extends AsyncTask<RegisterTaskParams, Integer, RegisterTaskResult> {
        protected RegisterTaskResult doInBackground(RegisterTaskParams... params) {
            RegisterTaskParams parameters = params[0];
            RegisterTaskResult result = null;

            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(BASE_URL+"/user/create");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("user", parameters.username)
                        .appendQueryParameter("pwd", parameters.password);
                String query = builder.build().getEncodedQuery();

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

                urlConnection.connect();

                if (urlConnection.getResponseCode() == 400) {
                    result = new RegisterTaskResult(false, Error.ERR_NICK_USED);
                } else if (urlConnection.getResponseCode() == 200) {
                    result = new RegisterTaskResult(true, null);
                }
            } catch (IOException e) {
                e.printStackTrace();
                result = new RegisterTaskResult(false, Error.ERR_NETWORK);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            if (result == null) {
                result = new RegisterTaskResult(false, Error.ERR_UNEXPECTED);
            }
            return result;
        }
    }

    public static class LoginTaskParams {
        public final String username;
        public final String password;

        public LoginTaskParams(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    public static class LoginTaskResult {
        public final boolean success;
        public final Error error;

        public LoginTaskResult(boolean success, Error error) {
            this.success = success;
            this.error = error;
        }
    }


    public class LoginTask extends AsyncTask<LoginTaskParams, Integer, LoginTaskResult> {
        protected LoginTaskResult doInBackground(LoginTaskParams... params) {
            LoginTaskParams parameters = params[0];
            LoginTaskResult result = null;

            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(BASE_URL+"/user/login");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("user", parameters.username)
                        .appendQueryParameter("pwd", parameters.password);
                String query = builder.build().getEncodedQuery();

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

                urlConnection.connect();

                if (urlConnection.getResponseCode() == 400) {
                    result = new LoginTaskResult(false, Error.ERR_WRONG_CREDENTIALS);
                } else if (urlConnection.getResponseCode() == 200) {
                    result = new LoginTaskResult(true, null);
                    try {
                        // Assumes there is only one cookie and it's the token
                        HttpCookie cookie = cookieManager.getCookieStore().get(new URI(BASE_URL)).get(0);
                        if (BuildConfig.DEBUG) {
                            if (!cookie.getName().equals("token")) {
                                throw new AssertionError("Unexpected cookie");
                            }
                        }
                        String token = cookie.getValue();
                        prefs.edit().putString("token", token).apply();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                result = new LoginTaskResult(false, Error.ERR_NETWORK);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            if (result == null) {
                result = new LoginTaskResult(false, Error.ERR_UNEXPECTED);
            }

            return result;
        }
    }

    public static class GetCountsTaskResult {
        private final ArrayList<Count> counts;
        public final boolean success;

        public GetCountsTaskResult(boolean success, ArrayList<Count> counts) {
            if (counts != null) {
                this.counts = (ArrayList<Count>) counts.clone();
            } else {
                this.counts = null;
            }
            this.success = success;
        }

        public ArrayList<Count> getCounts() {
            return (ArrayList<Count>) counts.clone();
        }
    }

    public static class GetCountsTask extends AsyncTask<Void, Integer, GetCountsTaskResult> {
        protected GetCountsTaskResult doInBackground(Void... params) {
            GetCountsTaskResult result = null;

            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(BASE_URL+"/counts");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                urlConnection.connect();

                if (urlConnection.getResponseCode() == 200) {
                    JSONArray arr = new JSONArray(Utils.readInputStream(urlConnection.getInputStream()));
                    ArrayList<Count> counts = new ArrayList<>(arr.length());
                    for (int i = 0; i < arr.length(); i++)
                    {
                        counts.add(Count.fromJson(arr.getJSONObject(i)));
                    }
                    result = new GetCountsTaskResult(true, counts);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            if (result == null) {
                result = new GetCountsTaskResult(false, null);
            }
            return result;
        }
    }

    public static class SubscribeToCountTaskResult {
        public final boolean success;
        public final Error error;

        public SubscribeToCountTaskResult(boolean success, Error error) {
            this.success = success;
            this.error = error;
        }
    }

    public static class SubscribeToCountTaskParameters {
        public final String secret;

        public SubscribeToCountTaskParameters(String secret) {
            this.secret = secret;
        }
    }

    public static class SubscribeToCountTask extends AsyncTask<SubscribeToCountTaskParameters, Void, SubscribeToCountTaskResult> {

        @Override
        protected SubscribeToCountTaskResult doInBackground(SubscribeToCountTaskParameters... params) {
            SubscribeToCountTaskResult result = null;
            SubscribeToCountTaskParameters parameters = params[0];

            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(BASE_URL+"/counts/subscribe");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("secret", parameters.secret);
                String query = builder.build().getEncodedQuery();

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

                urlConnection.connect();

                if (urlConnection.getResponseCode() == 200) {
                    result = new SubscribeToCountTaskResult(true,  null);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            if (result == null) {
                result = new SubscribeToCountTaskResult(false, null);
            }
            return result;
        }
    }

    public static class CreateCountTaskResult {
        public final boolean success;
        public final Error error;

        public CreateCountTaskResult(boolean success, Error error) {
            this.success = success;
            this.error = error;
        }
    }

    public static class CreateCountTaskParameters {
        public final String name;

        public CreateCountTaskParameters(String name) {
            this.name = name;
        }
    }

    public static class CreateCountTask extends AsyncTask<CreateCountTaskParameters, Void, CreateCountTaskResult> {

        @Override
        protected CreateCountTaskResult doInBackground(CreateCountTaskParameters... params) {
            CreateCountTaskResult result = null;
            CreateCountTaskParameters parameters = params[0];

            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(BASE_URL+"/counts/create");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("name", parameters.name);
                String query = builder.build().getEncodedQuery();

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

                urlConnection.connect();

                if (urlConnection.getResponseCode() == 200) {
                    result = new CreateCountTaskResult(true,  null);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            if (result == null) {
                result = new CreateCountTaskResult(false, null);
            }
            return result;
        }
    }

    public enum Error {ERR_NETWORK, ERR_UNEXPECTED, ERR_NICK_USED, ERR_WRONG_CREDENTIALS};
}
