package com.example.instagappsm.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.instagappsm.R;
import com.example.instagappsm.adapter.CommentAdapter;
import com.example.instagappsm.model.Comments;
import com.example.instagappsm.model.IdPost;
import com.example.instagappsm.model.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

import java.time.chrono.JapaneseDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentActivity extends AppCompatActivity {

    private IdPost idPost;
    private FirebaseAuth auth;
    private RecyclerView recyclerViewComment;
    private Button btnAddComment;
    private EditText edtComment;
    private List<Comments> mList;
    private List<Users> usersList;
    private String currentUserId;
    private String post_id;
    private FirebaseFirestore firestore;
    private CommentAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        recyclerViewComment = findViewById(R.id.recycler_comments);
        btnAddComment = findViewById(R.id.btn_add_omment);
        edtComment = findViewById(R.id.edt_comment);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        post_id = getIntent().getStringExtra("postid");

        mList = new ArrayList<>();
        //usersList = new ArrayList<>();
        adapter = new CommentAdapter(mList, CommentActivity.this, post_id);


        recyclerViewComment.setHasFixedSize(true);
        recyclerViewComment.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewComment.setAdapter(adapter);

        firestore.collection("Posts/" + post_id + "/Comments").addSnapshotListener(CommentActivity.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentChange documentChange: value.getDocumentChanges()) {
                    if (documentChange.getType() == DocumentChange.Type.ADDED) {
                        Comments comments = documentChange.getDocument().toObject(Comments.class);
                        mList.add(comments);
                        adapter.notifyDataSetChanged();
                    }
                    else {
                        adapter.notifyDataSetChanged();
                    }
                };
            }
        });

        btnAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = edtComment.getText().toString();
                if(!comment.isEmpty()) {
                    Map<String , Object> commentsMap = new HashMap<>();
                    commentsMap.put("comment", comment);
                    commentsMap.put("time", FieldValue.serverTimestamp());
                    commentsMap.put("user", currentUserId);

                    firestore.collection("Posts/" + post_id + "/Comments").add(commentsMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(CommentActivity.this, "Comment Added !!", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(CommentActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(CommentActivity.this, "Please comment before add", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}