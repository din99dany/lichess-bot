package test;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface LichessApi {

    @FormUrlEncoded
    @Headers({"Authorization: Bearer bTJHDhdsGORSniDe"})
    @POST("challenge/{username}")
    Call<ResponseBody> challangeUser(@Path("username") String username,
                                     @Field("rated") String rated,
                                     @Field("clock.limit") String clockLimit,
                                     @Field("clock.increment") String clockIncrement);


    @Streaming
    @Headers({"Authorization: Bearer bTJHDhdsGORSniDe"})
    @GET("stream/event")
    Observable<ResponseBody> streamEvents();

    @Headers({"Authorization: Bearer bTJHDhdsGORSniDe"})
    @POST("challenge/{challengeId}/accept")
    Observable<ResponseBody> acceptChallenge( @Path("challengeId") String id );

    @Streaming
    @Headers({"Authorization: Bearer bTJHDhdsGORSniDe"})
    @GET("bot/game/stream/{gameId}")
    Observable<ResponseBody> streamGame( @Path("gameId") String gameId );

    @Headers({"Authorization: Bearer bTJHDhdsGORSniDe"})
    @POST("bot/game/{game_id}/move/{move}")
    Call<ResponseBody> makeMove( @Path("game_id") String game_id, @Path("move") String move );

}
