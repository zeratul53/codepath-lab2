package com.codepath.android.booksearch.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.android.booksearch.R;
import com.codepath.android.booksearch.models.Book;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BookDetailActivity extends AppCompatActivity {
    private ImageView ivBookCover;
    private TextView tvTitle;
    private TextView tvAuthor;
    private Toolbar toolbar;
    private ShareActionProvider shareAction;
    private Intent shareIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Fetch views
        ivBookCover = (ImageView) findViewById(R.id.ivBookCover);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvAuthor = (TextView) findViewById(R.id.tvAuthor);

        // Extract book object from intent extras
        Book book = (Book) getIntent().getParcelableExtra("book");

        // Use book object to populate data into views
        getSupportActionBar().setTitle(book.getTitle());
        tvTitle.setText(book.getTitle());
        tvAuthor.setText(book.getAuthor());
        Picasso.with(getApplicationContext()).load(book.getCoverUrl()).fit().centerCrop()
                .into(ivBookCover, new Callback() {
                    @Override
                    public void onSuccess() {
                        prepareShareIntent();
                        attachShareIntentAction();
                    }

                    @Override
                    public void onError() {
                        Log.d("ERROR", "Loading image error for book");
                    }
                });
    }

    public void prepareShareIntent() {
        // Fetch Bitmap Uri locally
        ImageView ivImage = (ImageView) findViewById(R.id.ivBookCover);
        Uri bmpUri = getLocalBitmapUri(ivImage); // see previous remote images section
        // Construct share intent as described above based on bitmap
        shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
        shareIntent.setType("image/*");
    }

    // Attaches the share intent to the share menu item provider
    public void attachShareIntentAction() {
        if (shareAction != null && shareIntent != null)
            shareAction.setShareIntent(shareIntent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_book_detail, menu);

        MenuItem item = menu.findItem(R.id.menu_item_share);
        // Fetch reference to the share action provider
        shareAction = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        attachShareIntentAction();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    // Returns the URI path to the Bitmap displayed in specified ImageView
    public Uri getLocalBitmapUri(ImageView imageView) {
        // Extract Bitmap from ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable){
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            // Use methods on Context to access package-specific directories on external storage.
            // This way, you don't need to request external read/write permission.
            // See https://youtu.be/5xVh-7ywKpE?t=25m25s


            // getExternalFilesDir() + "/Pictures" should match the declaration in fileprovider.xml paths
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");


            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();

//            // **Warning:** This will fail for API >= 24, use a FileProvider as shown below instead.
//            bmpUri = Uri.fromFile(file);

            // wrap File object into a content provider. NOTE: authority here should match authority in manifest declaration
            bmpUri = FileProvider.getUriForFile(BookDetailActivity.this, "com.codepath.fileprovider", file);

//        } catch (IOException e) {
//            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bmpUri;
    }
}
