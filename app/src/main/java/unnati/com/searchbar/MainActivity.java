package unnati.com.searchbar;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnKeyListener{

    private boolean processed = false;
    private List<Item> items = null;
    private EditText searchEditText;
    private Button searchButton;
    private ListView listView;
    private ItemAdapter adapter;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchEditText = (EditText) findViewById(R.id.editText);
        searchButton = (Button)findViewById(R.id.searchButton);
        listView = (ListView)findViewById(R.id.listView);

        //getting the user entered search product and looking it up through  doSearchQueryTask
        searchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                context = getApplicationContext();
                //Get the product from the search bar
                String product = searchEditText.getText().toString().trim();
                if(product.length() == 0){
                    searchEditText.setError("Please enter the product name");
                }
                else{
                    DoSearchQueryTask searchQueryTask = new DoSearchQueryTask();
                    searchQueryTask.execute(product);
                }

            }
        });

        //creating an intent and forwarding details of the clicked item to recommendation activity
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), RecommendationActivity.class);
                //Pass the id of the selected item to the next activity
                intent.putExtra("ID",items.get(i).getItemId());
                intent.putExtra("title",items.get(i).getTitle());
                intent.putExtra("price", items.get(i).getPrice());
                intent.putExtra("description", items.get(i).getDescription());
                intent.putExtra("image", items.get(i).getImage());

                startActivity(intent);
            }
        });

        //giving functionality to enter key in keyboard
        searchEditText.setOnKeyListener(this);

        }


    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent){
        //if pressed key is ENTER , do what a search button does
        if(i== keyEvent.KEYCODE_ENTER && keyEvent.getAction()== KeyEvent.ACTION_DOWN){

            DoSearchQueryTask searchQueryTask = new DoSearchQueryTask();
            searchQueryTask.execute(searchEditText.getText().toString().trim());

        }

        return false;
    }


    //Method to update the List
    private void setListUI(){
        //adapter = new ArrayAdapter<I>(getApplicationContext(), items, R.layout.single_item_layout,from, to);
        adapter = new ItemAdapter(getApplicationContext(), items);
        listView.setAdapter(adapter);
        //Item click listener for the list view

    }



    private class DoSearchQueryTask extends AsyncTask<String, Void, List<Item>> {

        private static final String API_KEY = "2wdn42b3afkwq2ekz4h6rzba";
        private static final String QUERY = "http://api.walmartlabs.com/v1/search?apiKey=%s&query=%s";

        @Override
        protected void onPostExecute(List<Item> itemList) {
            processed = true;
            items = itemList;
            if(items != null)
                setListUI();
        }

        @Override
        protected List<Item> doInBackground(String... strings) {

            //Getting the URL for the product
            String urlString = String.format(QUERY, API_KEY, strings[0]);

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String responseJSON = null;
            try {
                //Creating the URL
                URL url = new URL(urlString);
                //Connecting to the URL
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
                    // Stream was empty. No point in parsing.
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
            JSONObject jsonObject = new JSONObject(responseJSON);
            JSONArray jsonArray = (JSONArray) jsonObject.get("items");
            for(int i=0; i < jsonArray.length(); i++){
                Item item = getItemFromJsonObject((JSONObject)jsonArray.get(i));
                items.add(item);
                Log.d("Item",item.toString());
            }
            return items;

        }

        private Item getItemFromJsonObject(JSONObject o) throws Exception{
            String name = (o.getString("name"));
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
