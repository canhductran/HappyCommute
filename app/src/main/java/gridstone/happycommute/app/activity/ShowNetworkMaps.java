package gridstone.happycommute.app.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import gridstone.happycommute.app.R;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by Matt on 23/9/2014.
 * Last edited by Matt on 23/9/2014
 */

public class ShowNetworkMaps extends ActionBarActivity
{
    private ImageView mImageView;
    private PhotoViewAttacher mAttacher;
    private Drawable mapBitmap;
    private Menu mMenu;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#336699")));
        actionBar.setTitle(Html.fromHtml("<b><font color=\"#d3d3d3\">" + "Maps" + "</font></b>"));

        setContentView(R.layout.activity_train_map);

        mImageView = (ImageView) findViewById(R.id.network_map_imageview);
        Picasso.with(this).load(R.drawable.ptv_train_map).into(mImageView);
//        mAttacher.setScale
//        mAttacher = new PhotoViewAttacher(mImageView);
//        mAttacher.setScaleType(ImageView.ScaleType.FIT_CENTER);
//        mAttacher.setScale()

        setMapImage(R.drawable.ptv_train_map);
    }
    public boolean onCreateOptionsMenu(Menu menu)
    {
// Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.network_maps, menu);
        this.mMenu = menu;
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        MenuItem busButton = mMenu.getItem(0);
        MenuItem tramButton = mMenu.getItem(1);
        MenuItem trainButton = mMenu.getItem(2);
        MenuItem nightRiderButton = mMenu.getItem(3);

        busButton.setIcon(getResources().getDrawable(R.drawable.toggle_bus));
        tramButton.setIcon(getResources().getDrawable(R.drawable.toggle_tram));
        trainButton.setIcon(getResources().getDrawable(R.drawable.toggle_train));
        nightRiderButton.setIcon(getResources().getDrawable(R.drawable.toggle_nightrider));



        switch (item.getItemId())
        {
            case R.id.show_bus_map_button:
                busButton.setIcon(getResources().getDrawable(R.drawable.toggle_bus_selected));
//                setMapImage(R.drawable.ptv_bus_map);
                Picasso.with(this).load(R.drawable.ptv_bus_map).centerInside().resize(1044,1476).into(mImageView);
                mAttacher.setScale(1);
                break;
            case R.id.show_tram_map_button:
                tramButton.setIcon(getResources().getDrawable(R.drawable.toggle_tram_selected));
                Picasso.with(this).load(R.drawable.ptv_tram_map).centerInside().resize(1044,1476).into(mImageView);
                mAttacher.setScale(1);
                break;
            case R.id.show_train_map_button:
                trainButton.setIcon(getResources().getDrawable(R.drawable.toggle_train_selected));
                Picasso.with(this).load(R.drawable.ptv_train_map).centerCrop().resize(1044,982).into(mImageView);
                mAttacher.setScale(1);
                break;
            case R.id.show_nightrider_map_button:
                nightRiderButton.setIcon(getResources().getDrawable(R.drawable.toggle_nightrider_selected));
                Picasso.with(this).load(R.drawable.ptv_nightrider_map).centerInside().resize(1044,1095).into(mImageView);
                mAttacher.setScale(1);

                break;
        }
        return super.onOptionsItemSelected(item);
    }
//    //TODO: Make Asyncronos
    public void setMapImage(int imageDrawableInt)
    {
//        mapBitmap = null;
//        // Set the Drawable  to be displayed
//        mapBitmap= getResources().getDrawable(imageDrawableInt);
//        mImageView.setImageDrawable(mapBitmap);
        Picasso.with(this).load(imageDrawableInt).centerInside().resize(1044,982).into(mImageView);
        // Attach a PhotoViewAttacher, which takes care of all of the zooming functionality.
        mAttacher = new PhotoViewAttacher(mImageView);
    }


}
