package sg.edu.np.MulaSave.HomePage;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import sg.edu.np.MulaSave.R;
import sg.edu.np.MulaSave.User;

public class AddPostActivity extends AppCompatActivity {

    ImageView previewImage, closeButton;
    TextView postButton, postDesc;
    CardView previewCard;
    int code = 200;
    Post post;
    FirebaseDatabase databaseRef = FirebaseDatabase
            .getInstance("https://mad-ay22-p05-team-b-default-rtdb.asia-southeast1.firebasedatabase.app/");
    DatabaseReference databaseRefUser = databaseRef.getReference("user");
    DatabaseReference databaseRefPost = databaseRef.getReference("post");
    FirebaseUser usr = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        //create user object
        User user = new User();
        databaseRefUser.child(usr.getUid().toString()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    for (DataSnapshot ds:task.getResult().getChildren()){
                        if (ds.getKey().equals("uid")){
                            user.setUid(ds.getValue().toString());
                        }
                        if(ds.getKey().equals("email")){
                            user.setEmail(ds.getValue().toString());
                        }
                        if(ds.getKey().equals("username")){
                            user.setUsername(ds.getValue().toString());

                        }
                    }
                }
            }
        });//end of setting users

        post = new Post();//create new post object with no fields

        previewImage = findViewById(R.id.previewImage);
        previewImage.setVisibility(View.INVISIBLE);//hide the preview first as it is the default android icon placeholder
        closeButton = findViewById(R.id.closeButton);
        postButton = findViewById(R.id.postButton);
        postDesc = findViewById(R.id.postDesc);
        previewCard = findViewById(R.id.previewCard);


        previewCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgChooser();
            }
        });

        postButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                post.setCreatorUid(user.getUid());//set the creator uid (string object)
                String randomId = UUID.randomUUID().toString();//create uuid to be used as the post uuid and also the name of the imagefile
                post.setPostUuid(randomId);

                //set post desc and trim to remove extra spaces
                post.setPostDesc(postDesc.getText().toString().trim());

                Instant instant = Instant.now();
                post.setPostDateTime(instant.toString());

                if (post.getPostImageUrl() == null){
                    Toast.makeText(AddPostActivity.this,"Please upload an Image before proceeding",Toast.LENGTH_SHORT).show();
                }
                else if (post.getPostDesc().equals("")){
                    Toast.makeText(AddPostActivity.this,"Please insert a description",Toast.LENGTH_SHORT).show();
                }
                else{
                    //store the image to storage
                    storageRef.child("postpics/" + randomId + ".png").putFile(Uri.parse(post.getPostImageUrl()))
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    storageRef.child("postpics/" + randomId + ".png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            post.setPostImageUrl(uri.toString());//set the image url in post object
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i("AddPostActivity",e.toString());
                                }
                            });
                    databaseRefPost.child(randomId).setValue(post);//set the post into the user under posts
                    //show success and exit
                    Toast.makeText(AddPostActivity.this,"Upload Success!",Toast.LENGTH_SHORT).show();
                    finish();

                }
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {//close the activity and discard
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }//end of oncreate

    /**
     * The image chooser called when user wants to upload an image
     * Returns the onActivityResult to finish selection
     */
    private void imgChooser() {//choose image method
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Image"), code);
    }

    //get image and load to preview

    /**
     * This method is executed after the imgChooser method is executed, this method sets the image uri
     * to the tentative post object and sets the previewed image to the user.
     * @param requestCode generated and used to ensure the image is correct
     * @param resultCode generated and used to ensure that the result executed successfully
     * @param data data of the selected image to be used
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {//if result is ok
            if (requestCode == code) {//ensure request code is same
                //get url of image
                Uri selectedImgUri = data.getData();
                if (null != selectedImgUri) {
                    post.setPostImageUrl(selectedImgUri.toString());//set the post object image url
                    // update the preview image in the layout
                    //previewImage.setImageURI(selectedImgUri);
                    Picasso.get().load(selectedImgUri).fit().centerCrop().into(previewImage);
                    previewImage.setVisibility(View.VISIBLE);//set to visible
                }
            }
        }
    }//end of on activity result
}