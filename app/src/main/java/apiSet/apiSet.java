package apiSet;

import ResponseModel.responseModel;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface apiSet {

    @FormUrlEncoded
    @POST("register_teacher.php")
    Call<responseModel> addTeacher(
            @Field("name") String name,
            @Field("emailID") String emailID
    );

    @FormUrlEncoded
    @POST("qrG.php")
    Call<responseModel> addLecture(
            @Field("subject_name") String subject_name
    );

}
