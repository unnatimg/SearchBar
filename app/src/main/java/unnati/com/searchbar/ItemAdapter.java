package unnati.com.searchbar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ItemAdapter extends BaseAdapter {

    private List<Item> items;
    private Context context;

    public ItemAdapter(Context context, List<Item> items){
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = view;
        if(v == null) {
            LayoutInflater inflater = (LayoutInflater) (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
            v = inflater.inflate(R.layout.single_item_layout, viewGroup, false);
        }

        Item item = items.get(i);
        ImageView imageView = v.findViewById(R.id.imageView);
        TextView titleTextView = v.findViewById(R.id.titleView);
        TextView priceTextView = v.findViewById(R.id.priceView);
        TextView descriptionTextView = v.findViewById(R.id.descriptionView);
        titleTextView.setText(item.getTitle());
        priceTextView.setText(""+item.getPrice()+" $");
        descriptionTextView.setText(item.getDescription());
        //load the picture
        Glide.with(context).load(item.getImage()).into(imageView);

        return v;
    }
}
