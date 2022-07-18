package sg.edu.np.MulaSave.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import sg.edu.np.MulaSave.Memorydata;
import sg.edu.np.MulaSave.Product;
import sg.edu.np.MulaSave.R;

public class chat extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://mad-ay22-p05-team-b-default-rtdb.asia-southeast1.firebasedatabase.app/");
    DatabaseReference chatRef = database.getReference("chat");
    private FirebaseAuth mAuth;

    private String chatkey;
    String getuid= "";


    Product product;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_chat2);
        final ImageView backbtn = findViewById(R.id.backbtn);
        final TextView nameTTv = findViewById(R.id.name);
        final EditText messageedittxt = findViewById(R.id.messageedittxt);
        final ImageView profilepic = findViewById(R.id.profilePic);
        final ImageView sendbtn = findViewById(R.id.sendbtn);


        // Retrieving data from message adapater class

        Product productclass = (Product) getIntent().getSerializableExtra("product");//get product from adapter
        String sellerid = productclass.getSellerUid();

        final String getName = getIntent().getStringExtra("name");
        final String getprofilepic = getIntent().getStringExtra("Profilepic");
        chatkey = getIntent().getStringExtra("chatkey");
        final String uid = getIntent().getStringExtra("uid");

        //get user uid from memory
        //getuid = Memorydata.getdata(chat.this);
        getuid=mAuth.getCurrentUser().getUid();

        nameTTv.setText(getName);
        Picasso.get().load(getprofilepic).into(profilepic);
        chatkey = "";
        if (chatkey!= null){
            if(chatkey.isEmpty()){

                chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //generating chatkey by default chatkey is 1
                        chatkey = "1";
                        sendbtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                final String currenttimestamp = String.valueOf(System.currentTimeMillis()).substring(0,10);
                                final String gettextmessage = messageedittxt.getText().toString();

                                Memorydata.savelastmsgts(currenttimestamp,chatkey,chat.this);
                                chatRef.child(chatkey).child("user_1").setValue(getuid);
                                chatRef.child(chatkey).child("user_2").setValue(sellerid);
                                chatRef.child(chatkey).child("messages").child(currenttimestamp).child("msg").setValue(gettextmessage);
                                chatRef.child(chatkey).child("messages").child(currenttimestamp).child("uid").setValue(getuid);



                            }
                        });
                        if(snapshot.hasChild("chat")){
                            chatkey=(String.valueOf(snapshot.child("chat").getChildrenCount()+1));





                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }





        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}