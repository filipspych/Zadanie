package com.example.zadanie_20481024;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

public class DetailsActivity extends AppCompatActivity {
    private FirebaseAnalytics firebaseAnalytics;
    private FullPost fullPost;
    private ScrollView scrollView;
    private TextView authorTextView;
    private ProgressBar progressBar;
    private TextView errorTextView;
    private DetailActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        //analityka
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //ustawianie referencji
        scrollView = findViewById(R.id.scrollView);
        authorTextView = scrollView.findViewById(R.id.authorTextView);
        progressBar = findViewById(R.id.progressBar);
        errorTextView = findViewById(R.id.errorTextView);
        viewModel = ViewModelProviders.of(this).get(DetailActivityViewModel.class);

        //animacje
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.setDuration(2000);
        ((ViewGroup) findViewById(R.id.constraintLayout)).setLayoutTransition(layoutTransition);

        //pobieranie danych i wypełnianie Views
        int postId = getIntent().getIntExtra(MainActivity.EXTRA_POST_ID, 0);
        new LoadFullPost().execute(postId);

        //konfiguracja views
        authorTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{fullPost.getAuthorEmail()});
                i.putExtra(Intent.EXTRA_SUBJECT, "Follow-up question on your post: " + fullPost.getTitle());
                i.putExtra(Intent.EXTRA_TEXT   , "Dear author,");
                //analityka
                Bundle bundle = new Bundle();
                bundle.putString("post_id", String.valueOf(fullPost.getPostId()));
                bundle.putString("post_title", fullPost.getTitle());
                bundle.putString("author_email", fullPost.getAuthorEmail());
                bundle.putString("author_name", fullPost.getAuthor());
                firebaseAnalytics.logEvent("write_to_author", bundle);
                try {
                    startActivity(Intent.createChooser(i, "Send this author an email"));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(DetailsActivity.this, "There are no email clients installed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    /**
     * Ładuje dane. Może używać cache (cache ma pojemność jednego posta)
     * Uwaga: jest static ale nie ryzykujemy memory leak, bo model ma ustawiony timeout na połączenia URL.
     */
    @SuppressLint("StaticFieldLeak")
    private class LoadFullPost extends AsyncTask<Integer, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            scrollView.setVisibility(View.GONE);
            errorTextView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(Integer... integers) {
            fullPost = viewModel.getFullPost(integers[0]);
            if(fullPost != null)return 0; //poprawnie zakonczone dzialanie
            else return 1; //działanie zakonczone bledem
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(integer == 0){
                ((TextView)scrollView.findViewById(R.id.titleTextView)).setText(fullPost.getTitle());
                authorTextView.setText("by " + fullPost.getAuthor());
                ((TextView)scrollView.findViewById(R.id.bodyTextView)).setText(fullPost.getBody());
                LinearLayout rootLinearLayout = scrollView.findViewById(R.id.commentsLinearLayout);
                for(FullPost.Comment comment : fullPost.getComments()){
                    //for every comment I create a view, fill it with info and attach it to the linear layout inside the scroll view
                    LinearLayout commentLinearLayout = (LinearLayout) LayoutInflater.from(scrollView.getContext())
                            .inflate(R.layout.view_comment, rootLinearLayout, false);
                    ((TextView) commentLinearLayout.findViewById(R.id.nameTextView)).setText(comment.getName());
                    ((TextView) commentLinearLayout.findViewById(R.id.emailTextView)).setText(comment.getEmail());
                    ((TextView) commentLinearLayout.findViewById(R.id.commentBodyTextView)).setText(comment.getBody());
                    rootLinearLayout.addView(commentLinearLayout);

                }
                progressBar.setVisibility(View.GONE);
                scrollView.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(DetailsActivity.this, "Coś poszło nie tak. Przepraszamy.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                errorTextView.setVisibility(View.VISIBLE);
            }
        }
    }
}
