package com.example.zadanie_20481024;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FirebaseAnalytics firebaseAnalytics;
    public static final String EXTRA_POST_ID = BuildConfig.APPLICATION_ID +".POST_ID";

    private ProgressBar progressBar;
    private TextView errorTextView;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter postsAdapter;
    private LinearLayoutManager layoutManager;
    private MainActivityViewModel viewModel;
    private final List<PostSummary> postSummaries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //analityka
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        MainActivity.this.firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null);


        //ustawianie referencji
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        errorTextView = findViewById(R.id.errorTextView);
        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);


        //pobieranie danych
        new LoadPostSummaries().execute();

        //animacje
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.setStagger(LayoutTransition.CHANGE_APPEARING, 2000);
        layoutTransition.setStagger(LayoutTransition.CHANGE_DISAPPEARING, 2000);
        layoutTransition.setStagger(LayoutTransition.CHANGING, 2000);
        layoutTransition.setDuration(2000);
        ((ViewGroup) findViewById(R.id.frameLayout)).setLayoutTransition(layoutTransition);

        //konfigurowanie recycler view
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        postsAdapter = new PostsAdapter(postSummaries);
        recyclerView.setAdapter(postsAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    /**
     * Ładuje dane. Może używać cache.
     * Uwaga: jest static ale nie ryzykujemy memory leak, bo model ma ustawiony timeout na połączenia URL.
     */
    @SuppressLint("StaticFieldLeak")
    private class LoadPostSummaries extends AsyncTask<Void, Void, List<PostSummary>>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            recyclerView.setVisibility(View.GONE);
            errorTextView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<PostSummary> doInBackground(Void... voids) {
            return viewModel.getPostSummaries(false);
        }

        @Override
        protected void onPostExecute(List<PostSummary> tmp) {
            super.onPostExecute(tmp);
            if(tmp != null){
                postSummaries.clear();
                progressBar.setVisibility(View.GONE);
                postSummaries.addAll(tmp);
                postsAdapter.notifyDataSetChanged();
                recyclerView.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(MainActivity.this, "Coś poszło nie tak. Przepraszamy.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                errorTextView.setVisibility(View.VISIBLE);
            }
        }
    }
    class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostSummaryViewHolder>{
        private List<PostSummary> dataSet;


        class PostSummaryViewHolder extends RecyclerView.ViewHolder{
            private TextView title, body;
            private int id;
            PostSummaryViewHolder(@NonNull final LinearLayout itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.titleTextView);
                body = itemView.findViewById(R.id.bodyTextView);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //analityka
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, String.valueOf(id));
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, String.valueOf(title.getText()));
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "post");
                        MainActivity.this.firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                        Intent intent = new Intent(view.getContext(), DetailsActivity.class);
                        intent.putExtra(MainActivity.EXTRA_POST_ID, id);
                        view.getContext().startActivity(intent);
                    }
                });
            }
            void setTitle(String title){
                this.title.setText(title);
            }

            void setBody(String body){
                this.body.setText(body);
            }

            void setId(int id){
                this.id = id;
            }
        }

        PostsAdapter(List<PostSummary> postSummaries) {
            dataSet = postSummaries;
        }


        @NonNull
        @Override
        public PostSummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout view = (LinearLayout) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_summary, parent, false);
            return new PostSummaryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PostSummaryViewHolder holder, int position) {
            PostSummary postSummary = dataSet.get(position);
            holder.setTitle(postSummary.getTitle());
            holder.setBody(postSummary.getBody());
            holder.setId(postSummary.getId());
        }

        @Override
        public int getItemCount() {
            return dataSet.size();
        }

    }

}

