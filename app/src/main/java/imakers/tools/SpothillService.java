package imakers.tools;


import imakers.classes.SpotInitiation;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public interface SpothillService {
    //@POST("/login")
    //User login(@Query("username") String username, @Query("password") String password);

    @GET("/spot/{major}/{minor}/?hash=prototype_neogenia_cz")
    SpotInitiation spotInitiation(@Path("major") int major, @Path("minor") int minor);

    @GET("/spot/1/1?hash=prototype_neogenia_cz")
    String spot();

}