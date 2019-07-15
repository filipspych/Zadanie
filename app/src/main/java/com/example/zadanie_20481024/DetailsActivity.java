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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

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

        //animacje
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.setDuration(2000);
        ((ViewGroup) findViewById(R.id.constraintLayout)).setLayoutTransition(layoutTransition);

        //analityka
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //ustawianie referencji
        scrollView = findViewById(R.id.scrollView);
        authorTextView = scrollView.findViewById(R.id.authorTextView);
        progressBar = findViewById(R.id.progressBar);
        errorTextView = findViewById(R.id.errorTextView);
        viewModel = ViewModelProviders.of(this).get(DetailActivityViewModel.class);

        //pobieranie danych i wypełnianie Views
        int postId = getIntent().getIntExtra(MainActivity.EXTRA_POST_ID, 0);
        String postTitle = getIntent().getStringExtra(MainActivity.EXTRA_POST_TITLE);
        String postBody = getIntent().getStringExtra(MainActivity.EXTRA_POST_BODY);

        ((TextView)scrollView.findViewById(R.id.titleTextView)).setText(postTitle);
        ((TextView)scrollView.findViewById(R.id.bodyTextView)).setText(postBody);
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
        int commentsLoaded = 0;
        LinearLayout rootLinearLayout;
        View commentsTitleView;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            rootLinearLayout = scrollView.findViewById(R.id.commentsLinearLayout);
            commentsTitleView = scrollView.findViewById(R.id.commentsTitleTextView);
        }

        @Override
        protected Integer doInBackground(Integer... integers) {
            fullPost = viewModel.getFullPost(integers[0]);
            try {
                Thread.sleep(500);
                publishProgress(); //pokazujemy autora
                Thread.sleep(500);
                publishProgress(); //pokazujemy naglowek komentarzy
                for(int i = 0; i < 3 && i < fullPost.getComments().size(); i++){
                    Thread.sleep(500);
                    publishProgress(); //pokazujemy po jednym komentarzu (ale tylko pierwsze 3)
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(fullPost != null)return 0; //poprawnie zakonczone dzialanie
            else return 1; //działanie zakonczone bledem
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

            if(authorTextView.getVisibility()==View.GONE){
                authorTextView.setText("by " + fullPost.getAuthor());
                authorTextView.setVisibility(View.VISIBLE);
                ((FrameLayout) progressBar.getParent()).removeView(progressBar);
            } else  if (commentsTitleView.getVisibility() == View.GONE) {
                commentsTitleView.setVisibility(View.VISIBLE);
            } else {
                //for every comment I create a view, fill it with info and attach it to the linear layout inside the scroll view
                LinearLayout commentLinearLayout = (LinearLayout) LayoutInflater.from(scrollView.getContext())
                        .inflate(R.layout.view_comment, rootLinearLayout, false);
                ((TextView) commentLinearLayout.findViewById(R.id.nameTextView)).setText(fullPost.getComments().get(commentsLoaded).getName());
                ((TextView) commentLinearLayout.findViewById(R.id.emailTextView)).setText(fullPost.getComments().get(commentsLoaded).getEmail());
                ((TextView) commentLinearLayout.findViewById(R.id.commentBodyTextView)).setText(fullPost.getComments().get(commentsLoaded).getBody());
                rootLinearLayout.addView(commentLinearLayout);
                commentsLoaded++;
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(integer == 0){
                List<FullPost.Comment> tmp = new ArrayList<>(fullPost.getComments());
                tmp.removeAll(fullPost.getComments().subList(0, commentsLoaded));
                for(FullPost.Comment comment : tmp){
                    //for every comment I create a view, fill it with info and attach it to the linear layout inside the scroll view
                    LinearLayout commentLinearLayout = (LinearLayout) LayoutInflater.from(scrollView.getContext())
                            .inflate(R.layout.view_comment, rootLinearLayout, false);
                    ((TextView) commentLinearLayout.findViewById(R.id.nameTextView)).setText(comment.getName());
                    ((TextView) commentLinearLayout.findViewById(R.id.emailTextView)).setText(comment.getEmail());
                    ((TextView) commentLinearLayout.findViewById(R.id.commentBodyTextView)).setText(comment.getBody());
                    rootLinearLayout.addView(commentLinearLayout);
                }
            } else {
                Toast.makeText(DetailsActivity.this, "Coś poszło nie tak. Przepraszamy.", Toast.LENGTH_SHORT).show();
                ((FrameLayout) progressBar.getParent()).removeView(progressBar);
                errorTextView.setVisibility(View.VISIBLE);
            }
        }
    }
}
