package kollus.test.media;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;

import com.google.gson.JsonObject;
import com.jayway.jsonpath.JsonPath;
import kollus.test.media.R;

import kollus.test.media.constant.KollusConstant;
import kollus.test.media.kollusapi.Executor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class NativeActivity extends KollusBaseActivity {
    private static final String TAG = NativeActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private RecyclerView.Adapter recyclerViewAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<RecyclerAdapter.CardData> items;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        items = new ArrayList<>();
        onSearch();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);


        Log.i(TAG, String.format("item size : %d", items.size()));


    }

    public void onSearch() {
        Call<JsonObject> res = Executor.getInstance().getService().getContentList(KollusConstant.KOLLUS_API_KEY, KollusConstant.KOLLUS_CHANNEL_KEY);
        res.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                List<HashMap<String, Object>> contentList = (List<HashMap<String, Object>>) JsonPath.parse(response.body().toString()).read("$.result.items.item.*");
                for (HashMap<String, Object> content : contentList) {
                    RecyclerAdapter.CardData cardData = new RecyclerAdapter.CardData(String.format("https://v.kr.kollus.com/poster/%s", content.get("media_content_key").toString()),
                            content.get("title").toString(), content.get("media_content_key").toString());
                    Log.i(TAG, cardData.toString());
                    items.add(cardData);
                }
                Log.i(TAG, String.format("item size : %d", items.size()));
                recyclerViewAdapter = new RecyclerAdapter(getApplicationContext(), items, R.layout.activity_native);
                recyclerView.setAdapter(recyclerViewAdapter);;
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, t.getMessage());

            }
        });
    }
}
