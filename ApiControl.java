package test;

import com.google.gson.Gson;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.Objects;

public class ApiControl {

    private Retrofit retrofit;
    private LichessApi lichessApi;

    public ApiControl() {

        retrofit = new Retrofit
                .Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .baseUrl("https://lichess.org/api/")
                .build();

        lichessApi = retrofit.create(LichessApi.class);

    }

    public void sendDanyRequest() {

        Call<ResponseBody> call = lichessApi.challangeUser("testdanielplayer", "false", "300", "15");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    String strrr = new Gson().toJson(response.body());
                    System.out.println(strrr);
                } else {
                    System.out.println(response.message() + "|" + response.code());
                    System.out.println(call.request().headers().toString());
                    System.out.println(call.request().url());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                System.out.print("-> something went wrong: " + t.getMessage().toString());
            }

        });
    }


    public void streamEvents() {

        lichessApi
                .streamEvents()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(responseBody -> events(responseBody.source()))
                .map(t -> t.length() > 0 ? t : "{}")
                .map(JSONObject::new)
                .groupBy(event -> event.has("type") ? event.get("type") : "")
                .subscribe(stringJson -> {
                    if (Objects.equals(stringJson.getKey(), "challenge")) {
                        stringJson.subscribe(challenge -> {
                            lichessApi
                                    .acceptChallenge((String) challenge.getJSONObject("challenge").get("id"))
                                    .subscribe(t -> {

                                        System.out.println(challenge.get("type") + " " + challenge.getJSONObject("challenge").get("id"));

                                    });

                        });
                    } else if (stringJson.getKey().equals("gameStart")) {

                        stringJson.subscribe(t ->
                        {
                            final String game_id = (String) t.getJSONObject("game").get("id");
                            AlphaBetaChess.main(null);
                            lichessApi.streamGame( (String) t.getJSONObject("game").get("id") )
                                    .flatMap(events -> events(events.source()))
                                    .subscribe(g -> {
                                        String belea = AlphaBetaChess.makeJsonMove(g);
                                        if ( belea != null ) {

                                            Call<ResponseBody> calll = lichessApi.makeMove( game_id, belea );
                                            calll.execute();
                                        }
                                    }, te -> {
                                        System.out.println("error: " + te.getMessage());
                                    });
                        });
                    } else {

                        stringJson.subscribe(t -> {
                            System.out.println(t.has("type") ? t.get("type") : "garbage");
                        });
                    }
                });

    }

    public Observable<String> events(BufferedSource source) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> subscriber) throws Throwable {
                while (!source.exhausted()) {
                    subscriber.onNext(source.readUtf8Line());
                }
                subscriber.onComplete();
            }
        });
    }

}


