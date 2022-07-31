package Controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import apiSet.apiSet;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class controller {
    private static final String URl = "http://192.168.195.161/smart_attendance/";
    private static controller clientObj;
    private static Retrofit retrofit;
    

    controller() {
        retrofit = new Retrofit.Builder()
                .baseUrl(URl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized controller getInstance() {
        if (clientObj == null)
            clientObj = new controller();
        return clientObj;

    }

    public apiSet getAPI(){
        return retrofit.create(apiSet.class);
    }

}
