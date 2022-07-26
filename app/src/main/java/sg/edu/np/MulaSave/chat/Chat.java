package sg.edu.np.MulaSave.chat;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import sg.edu.np.MulaSave.MemoryData;
import sg.edu.np.MulaSave.Product;
import sg.edu.np.MulaSave.R;
import sg.edu.np.MulaSave.messages.MessageListener;

public class Chat extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://mad-ay22-p05-team-b-default-rtdb.asia-southeast1.firebasedatabase.app/");
    DatabaseReference chatRef = database.getReference("Chat");
    DatabaseReference cbRef = database.getReference();
    DatabaseReference userRef = database.getReference("user");
    FirebaseUser usr = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseAuth mAuth;

    private final List<ChatListener> chatlistnerList = new ArrayList<>();
    private ChatAdapter chatadapter;

    String chatkey = "0";
    String getuid = "";
    String username = "";
    String sellerid = "";
    MessageListener messageListener;
    private boolean loadingfirsttime = true;

    private RecyclerView chattingrecycleview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_chat);
        final ImageView backbtn = findViewById(R.id.backbtn);
        final TextView nameTTv = findViewById(R.id.name);
        final EditText messageedittxt = findViewById(R.id.messageedittxt);
        final ImageView profilepic = findViewById(R.id.profilePic);
        final ImageView sendbtn = findViewById(R.id.sendbtn);
        chattingrecycleview = findViewById(R.id.chattingrecycleview);


        sellerid = getIntent().getStringExtra("sellerid");
        // Retrieving data from message adapater class

        messageListener = (MessageListener) getIntent().getSerializableExtra("messageListener");

        DatabaseReference mDatabase;
        mDatabase = database.getReference("user");
        Log.v("selleriddd", sellerid);
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    if(dataSnapshot.getKey().toString().equals(sellerid+usr.getUid()) || dataSnapshot.getKey().toString().equals(usr.getUid()+sellerid)){
                        chatkey = dataSnapshot.getKey().toString();
                        break;
                    }
                    Log.i("knn",dataSnapshot.getKey().toString());
                }
                //no existing chat, create new chat key
                if(chatkey.equals("0")){
                    chatkey = usr.getUid()+sellerid;
                }
                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            if(ds.child("uid").getValue(String.class).equals(sellerid)){
                                FirebaseStorage storage = FirebaseStorage.getInstance();
                                StorageReference storageRef = storage.getReference();
                                username = ds.child("username").getValue(String.class);
                                nameTTv.setText(username);
                                storageRef.child("profilepics/" + ds.child("uid").getValue().toString() + ".png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {//user has set a profile picture before
                                        Picasso.get().load(uri).into(profilepic);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {//file does not exist (user did not upload before)
                                    @Override
                                    public void onFailure(@NonNull Exception e) {//set default picture

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                final String getprofilepic = getIntent().getStringExtra("Profilepic");

                final String uid = getIntent().getStringExtra("uid");

                //getting uid
                getuid = mAuth.getCurrentUser().getUid();




                chattingrecycleview.setHasFixedSize(true);
                chattingrecycleview.setLayoutManager(new LinearLayoutManager(Chat.this));

                chatadapter = new ChatAdapter(chatlistnerList, Chat.this);
                chattingrecycleview.setAdapter(chatadapter);



                //Creating the timestamp and
                cbRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (chatkey != null) {
                            if (chatkey.isEmpty()) {
                                //generating chatkey by default chatkey is 1
                                if (snapshot.hasChild("Chat")) {
                                    chatkey = (String.valueOf(snapshot.child("Chat").getChildrenCount() + "1"));
                                }
                            }
                        }

                        if (snapshot.hasChild("Chat")) {
                            if (snapshot.child("Chat").child(chatkey).hasChild("messages")) {
                                chatlistnerList.clear();
                                for (DataSnapshot messagesnapshot : snapshot.child("Chat").child(chatkey).child("messages").getChildren()) {
                                    if (messagesnapshot.hasChild("msg") && messagesnapshot.hasChild("uid")) {
                                        final String messagetimestamp = messagesnapshot.getKey();
                                        final String getuid = messagesnapshot.child("uid").getValue(String.class);
                                        final String getmsg = messagesnapshot.child("msg").getValue(String.class);
                                        Long timestampmili = Long.parseLong(messagetimestamp);

                                        Timestamp timestamp = new Timestamp(timestampmili);
                                        Date date = new java.util.Date(timestamp.getTime());
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                        SimpleDateFormat simpletimeFormat = new SimpleDateFormat("hh:mm:aa", Locale.getDefault());
                                        Log.v("date",simpleDateFormat.format(date));


                                        ChatListener Chatlistner = new ChatListener(getuid,username,getmsg,simpleDateFormat.format(date),simpletimeFormat.format(date));
                                        chatlistnerList.add(Chatlistner);

                                        if (loadingfirsttime || Long.parseLong(messagetimestamp) > Long.parseLong(MemoryData.getlastmsgts(Chat.this, chatkey))) {
                                            loadingfirsttime = false;
                                            MemoryData.savelastmsgts(messagetimestamp, chatkey, Chat.this);
                                            chatadapter.updatechatlist(chatlistnerList);
                                            chattingrecycleview.scrollToPosition(chatlistnerList.size() - 1);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                // sending messages and it will be written in the firebase
                sendbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // creating the time stamp
                        final String currenttimestamp = String.valueOf(System.currentTimeMillis());
                        final String gettextmessage = messageedittxt.getText().toString();

                        MemoryData.savelastmsgts(currenttimestamp, chatkey, Chat.this);

                        chatRef.child(chatkey).child("user_1").setValue(getuid);
                        chatRef.child(chatkey).child("user_2").setValue(sellerid);
                        chatRef.child(chatkey).child("messages").child(currenttimestamp).child("msg").setValue(gettextmessage);
                        chatRef.child(chatkey).child("messages").child(currenttimestamp).child("uid").setValue(getuid).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                chatadapter.notifyDataSetChanged();
                            }
                        });
                        //clear edit text
                        messageedittxt.setText("");
                    }

                });
                //Going back to the previous page
                backbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}

