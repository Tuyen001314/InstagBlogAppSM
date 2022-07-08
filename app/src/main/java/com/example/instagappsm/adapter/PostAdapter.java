package com.example.instagappsm.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagappsm.R;
import com.example.instagappsm.activity.CommentActivity;
import com.example.instagappsm.activity.OtherUserProfile;
import com.example.instagappsm.model.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private List<Post> listPost;
    private Activity context;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    public PostAdapter(Activity context, List<Post> listPost) {
        this.listPost = listPost;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.each_post, parent, false);
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Post post = listPost.get(position);

        holder.setPostPic(post.getImage());
        holder.setPostCaption(post.getCaption());

        long milliseconds = post.getTime().getTime();
        String date = DateFormat.format("MM/dd/yyyy", new Date(milliseconds)).toString();
        holder.setPostDate(date);

        String userId = post.getUser();
        firestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    String username = task.getResult().getString("name");
                    String image = task.getResult().getString("image");

                    holder.setPostUsername(username);
                    holder.setProfilePic(image);

                    holder.postUsername.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, OtherUserProfile.class);
                            intent.putExtra("name", username);
                            intent.putExtra("image", image);
                            context.startActivity(intent);
                        }
                    });

                    holder.profilePic.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, OtherUserProfile.class);
                            intent.putExtra("name", username);
                            intent.putExtra("image", image);
                            context.startActivity(intent);
                        }
                    });
                }
                else {
                    Toast.makeText(context, task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        String postId = post.PostId;
        String curentUserId = auth.getCurrentUser().getUid();

        firestore.collection("Posts/" + postId + "/Likes").document(curentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                firestore.collection("Posts/" + postId + "/Likes").document(curentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error == null) {
                            if(value.exists()) {
                                holder.likePic.setImageDrawable(context.getDrawable(R.drawable.affer_liked));
                            }
                            else {
                                holder.likePic.setImageDrawable(context.getDrawable(R.drawable.before_liked));
                            }
                        }
                    }
                });

                firestore.collection("Posts/" + postId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error == null) {
                            if(!value.isEmpty()) {
                                int count = value.size();
                                holder.setPostLikes(count);
                            }
                            else {
                                holder.setPostLikes(0);
                            }
                        }
                    }
                });
            }
        });

        if(curentUserId.equals(post.getUser())) {
            holder.deleteBtn.setVisibility(View.VISIBLE);
            holder.deleteBtn.setClickable(true);
            holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    AlertDialog.Builder alert = new AlertDialog.Builder(context, 4);
                    alert.setTitle("Delete").setMessage("Are You Sure ?")
                            .setNegativeButton("No", null)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    firestore.collection("Posts/" + postId + "/Comments").get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                                        firestore.collection("Posts/" + postId + "/Comments").document(snapshot.getId()).delete();
                                                    }
                                                }
                                            });
                                    firestore.collection("Posts/" + postId + "/Likes").get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                                        firestore.collection("Posts/" + postId + "/Likes").document(snapshot.getId()).delete();
                                                    }
                                                }
                                            });
                                    firestore.collection("Posts").document(postId).delete();
                                    listPost.remove(position);
                                    notifyDataSetChanged();
                                }
                            });
                    alert.show();
                }
            });
        }
        else {
            holder.deleteBtn.setVisibility(View.INVISIBLE);
            holder.deleteBtn.setClickable(false);

        }

        holder.likePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firestore.collection("Posts/" + postId + "/Likes").document(curentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(!task.getResult().exists()) {
                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());
                            firestore.collection("Posts/" + postId + "/Likes").document(curentUserId).set(likesMap);
                        }
                        else {
                            firestore.collection("Posts/" + postId + "/Likes").document(curentUserId).delete();
                        }

                        firestore.collection("Posts/" + postId + "/Likes").document(curentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                                if(error == null) {
                                    if(value.exists()) {
                                        holder.likePic.setImageDrawable(context.getDrawable(R.drawable.affer_liked));
                                    }
                                    else {
                                        holder.likePic.setImageDrawable(context.getDrawable(R.drawable.before_liked));
                                    }
                                }
                            }
                        });

                        firestore.collection("Posts/" + postId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                if(error == null) {
                                    if(!value.isEmpty()) {
                                        int count = value.size();
                                        holder.setPostLikes(count);
                                    }
                                    else {
                                    }
                                }
                            }
                        });
                    }
                });
            }
        });

        holder.commemtsPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent commentIntent = new Intent(context, CommentActivity.class);
                commentIntent.putExtra("postid", postId);
                context.startActivity(commentIntent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return listPost.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView postPic, commemtsPic, likePic;
        CircleImageView profilePic;
        TextView postUsername, postDate, postCaption, postLikes;
        View mView;
        ImageButton deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            likePic = itemView.findViewById(R.id.img_view_like);
            commemtsPic = itemView.findViewById(R.id.img_view_comment);
            deleteBtn = itemView.findViewById(R.id.image_button_delete);

        }

        public void setPostLikes(int count) {
            postLikes = mView.findViewById(R.id.tv_count_like);
            postLikes.setText(count + " Likes");
        }

        public void setPostPic(String url) {
            postPic = mView.findViewById(R.id.user_post);
            Glide.with(context).load(url).into(postPic);
        }
        public void setProfilePic(String url) {
            profilePic = mView.findViewById(R.id.profile_pic);
            Glide.with(context).load(url).into(profilePic);
        }
        public void setPostUsername(String username) {
            postUsername = itemView.findViewById(R.id.tv_username);
            postUsername.setText(username);
        }
        public void setPostDate(String date) {
            postDate = itemView.findViewById(R.id.tv_date);
            postDate.setText(date);
        }
        public void setPostCaption(String caption) {
            postCaption = itemView.findViewById(R.id.tv_caption);
            postCaption.setText(caption);
        }
    }
}
