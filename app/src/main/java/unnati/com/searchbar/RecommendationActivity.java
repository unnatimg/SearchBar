package unnati.com.searchbar;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RecommendationActivity extends AppCompatActivity {

    private long id;
    private String title;
    private String image;
    private String description;
    private double price;

    private TextView titleView;
    private TextView priceView;
    private TextView descriptionView;
    private ImageView imageView;
    private ListView listView;
    private ItemAdapter adapter;
    private Button cartDummyButton;

    private List<Item> items;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation);
        Intent intent = getIntent();
        id = intent.getLongExtra("ID",0);
        title = intent.getStringExtra("title");
        price = intent.getDoubleExtra("price",0);
        description = intent.getStringExtra("description");
        image = intent.getStringExtra("image");

        //Setting the contents
        titleView = findViewById(R.id.titleTextView);
        priceView = findViewById(R.id.priceTextView);
        descriptionView = findViewById(R.id.descriptionTextView);
        descriptionView.setMovementMethod(new ScrollingMovementMethod());
        imageView = findViewById(R.id.imageView2);
        listView = findViewById(R.id.recommendedListView);
        cartDummyButton=findViewById(R.id.cartDummyButton);

        titleView.setText(title);
        priceView.setText(price+" $");
        descriptionView.setText(description);
        //Load the image
        Glide.with(getApplicationContext()).load(image).into(imageView);
        //Load the recommended products
        DoRecommendationsQueryTask recommendationsQueryTask = new DoRecommendationsQueryTask();
        recommendationsQueryTask.execute(id);

        cartDummyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(RecommendationActivity.this, "Item added to cart!", Toast.LENGTH_LONG).show();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Intent intent = new Intent(RecommendationActivity.this , RecommendationActivity.class);
            //Pass the id of the selected item to the next activity
            intent.putExtra("ID",items.get(i).getItemId());
            intent.putExtra("title",items.get(i).getTitle());
            intent.putExtra("price", items.get(i).getPrice());
            intent.putExtra("description", items.get(i).getDescription());
            intent.putExtra("image", items.get(i).getImage());

                startActivity(intent);
            }
        });

    }

    //Method to update the List
    private void setListUI(){
        //adapter = new ArrayAdapter<I>(getApplicationContext(), items, R.layout.single_item_layout,from, to);
        adapter = new ItemAdapter(getApplicationContext(), items);
        listView.setAdapter(adapter);

    }

    private class DoRecommendationsQueryTask extends AsyncTask<Long, Void, List<Item>> {

        private static final String API_KEY = "2wdn42b3afkwq2ekz4h6rzba";
        private static final String QUERY = "http://api.walmartlabs.com/v1/nbp?apiKey=%s&itemId=%s";

        @Override
        protected void onPostExecute(List<Item> itemList) {
            items = itemList;
            if(items != null)
                setListUI();
        }

        @Override
        protected List<Item> doInBackground(Long... longs) {

            //Getting the URL for the product
            String urlString = String.format(QUERY, API_KEY, longs[0]);

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String responseJSON = null;
            try {
                //Creating the URL
                URL url = new URL(urlString);
                //Connect to the URL
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");


                urlConnection.connect();

                // Reading the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line);
                    buffer.append("\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                responseJSON = buffer.toString();
                Log.e("API Search Response", responseJSON);

            }catch (IOException e){
                e.printStackTrace();
            }
            try {
                return parseData(responseJSON);
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        private List<Item> parseData(String responseJSON) throws Exception{
            ArrayList<Item> items = new ArrayList<>();
            //JSONObject jsonObject = new JSONObject(responseJSON);
            JSONArray jsonArray = new JSONArray(responseJSON);
            for(int i=0; i < jsonArray.length(); i++){
                Item item = getItemFromJsonObject((JSONObject)jsonArray.get(i));
                items.add(item);
                Log.d("Item",item.toString());
            }
            return items;

        }

        private Item getItemFromJsonObject(JSONObject o) throws Exception{
            //myString.replaceAll("#\?.*?;", "");
            String name = removeTags(o.getString("name"));
            long itemId = o.getLong("itemId");
            double price = o.getDouble("salePrice");
            String description = removeTags(o.getString("shortDescription").replaceAll("&\\?.*?;",""));
            String imageURL = o.getString("thumbnailImage");

            return new Item(itemId,name, imageURL, price, description);

        }

        private String removeTags(String string) {
            string = (string.replaceAll("\\&.*?\\;",""));
            string = (string.replaceAll("ul",""));
            string = string.replaceAll("li","");
            string = string.replaceAll("class=","");
            string = string.replaceAll("noindent","");
            return string;
        }
    }
}
