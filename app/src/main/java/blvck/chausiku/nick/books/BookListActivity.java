package blvck.chausiku.nick.books;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class BookListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{
    private ProgressBar mLoadingProgress;
    private RecyclerView rv_books;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);
        mLoadingProgress = (ProgressBar) findViewById(R.id.pb_loading);

        Intent intent = getIntent();
        String query = intent.getStringExtra("Query");
        URL bookUrl;
        rv_books = (RecyclerView) findViewById(R.id.rv_books);
        LinearLayoutManager bookLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rv_books.setLayoutManager(bookLayoutManager);
        try {
            if(query==null || query.isEmpty()){
                bookUrl = APIUtil.baseUrl("cooking");
            }
            else {
                bookUrl = new URL(query);
            }
            new BooksQueryTask().execute(bookUrl);
        }
        catch(Exception e)
        {
            Log.d("Error", e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.book_list_menu, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        ArrayList<String> recentList = SpUtil.getQueryList(getApplicationContext());
        int itemNum = recentList.size();
        MenuItem recentMenu;
        for(int i=0; i<itemNum; i++){
            recentMenu = menu.add(Menu.NONE, i, Menu.NONE,recentList.get(i));
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_advanced_search:
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                return true;

            default:
                int position = item.getItemId() + 1;
                String preferenceName = SpUtil.QUERY + String.valueOf(position);
                String query = SpUtil.getPreferenceString(getApplicationContext(), preferenceName);
                String[] prefParams = query.split("\\,");
                String[] queryParams = new String[4];
                for(int i=0; i<prefParams.length; i++){
                    queryParams[i] =prefParams[i];
                }
                URL bookUrl = APIUtil.baseUrl(
                        (queryParams[0]==null)?"":queryParams[0],
                        (queryParams[1]==null)?"":queryParams[1],
                        (queryParams[2]==null)?"":queryParams[2],
                        (queryParams[3]==null)?"":queryParams[3]
                        );

                new BooksQueryTask().execute(bookUrl);

                 return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        try
        {
            URL bookUrl = APIUtil.baseUrl(query);
            new BooksQueryTask().execute(bookUrl);
        }
        catch(Exception e)
        {
            Log.d("Error", e.getMessage());
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    public class BooksQueryTask extends AsyncTask<URL, Void, String>{
        @Override
        protected String doInBackground(URL... urls) {
            URL searchBooks = urls[0];
            String result = null;

            try
            {
                result = APIUtil.getJSON(searchBooks);
            }
            catch (IOException e)
            {
                Log.e("Error", e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {

            TextView tvError = (TextView) findViewById(R.id.tv_error);
            mLoadingProgress.setVisibility(View.INVISIBLE);
            if(result == null)
            {
                rv_books.setVisibility(View.INVISIBLE);
                tvError.setVisibility(View.VISIBLE);
            }
            else
                {
                    rv_books.setVisibility(View.VISIBLE);
                    tvError.setVisibility(View.INVISIBLE);
                    ArrayList<Book> books = APIUtil.getBooksFromJson(result);
                    String resultString = "";
                    BooksAdapter adapter = new BooksAdapter(books);
                    rv_books.setAdapter(adapter);
                }
        }

        @Override
        protected void onPreExecute() {
            mLoadingProgress.setVisibility(View.VISIBLE);
        }
    }
}
