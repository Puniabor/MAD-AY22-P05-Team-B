package sg.edu.np.MulaSave;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import sg.edu.np.MulaSave.messages.messageadapter;
import sg.edu.np.MulaSave.messages.messagelistiner;

public class chatfeaturetesting extends AppCompatActivity {
    private final List<messagelistiner> messagelistinerList = new ArrayList<>();
    private String uid;
    private String username;
    private RecyclerView messagerecycleview;
    private ImageView userprofilepic;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://mad-ay22-p05-team-b-default-rtdb.asia-southeast1.firebasedatabase.app/");
    DatabaseReference userRef = database.getReference("user");
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);




        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading");
        progressDialog.show();



        if (user!=null){
            userRef.addValueEventListener(new ValueEventListener() {
                                              @Override
                                              public void onDataChange(DataSnapshot snapshot) {
                                                  String username;
                                                  if (snapshot.child(user.getUid()).child("username").exists()) { //Check if username exists to prevent crash
                                                      username = snapshot.child(user.getUid()).child("username").getValue().toString();
                                                  } else {
                                                      username = "";
                                                  }
                                                  storageRef.child("profilepics/" + user.getUid().toString() + ".png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                      @Override
                                                      public void onSuccess(Uri uri) {//user has set a profile picture before
                                                          userprofilepic = findViewById(R.id.userprofilepic1);
                                                          Picasso.get().load(uri).into(userprofilepic);
                                                          progressDialog.dismiss();

                                                      }
                                                  }).addOnFailureListener(new OnFailureListener() {//file does not exist (user did not upload before)
                                                      @Override
                                                      public void onFailure(@NonNull Exception e) {//set default picture
                                                          progressDialog.dismiss();

                                                      }
                                                  });
                                              }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressDialog.dismiss();
                }
            });
        }



        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagelistinerList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    final String getuid = dataSnapshot.getKey();
                    Log.v("getuid; ", getuid);
                    final String getname = dataSnapshot.child("username").getValue(String.class);
                    final String getprofilepic = storageRef.child("profilepics/" + user.getUid().toString() + ".png").getDownloadUrl().toString();


                    //must chage lastmessage
                    messagelistiner messagelistiners = new messagelistiner(getname,getuid,"",getprofilepic,0);

                    if (messagelistiners.getLastmessage()!= ""){
                        messagelistinerList.add(messagelistiners);

                    }
                }
                messagerecycleview.setAdapter(new messageadapter(messagelistinerList,chatfeaturetesting.this));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        setContentView(R.layout.activity_chatfeaturetesting);
        messagerecycleview=findViewById(R.id.messgaerecycleview);
        user.getUid(); //own uid
        messagerecycleview.setHasFixedSize(true);
        messagerecycleview.setLayoutManager(new LinearLayoutManager(this));





    }
}