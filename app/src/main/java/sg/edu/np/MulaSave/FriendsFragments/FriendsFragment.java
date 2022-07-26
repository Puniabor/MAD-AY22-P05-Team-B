package sg.edu.np.MulaSave.FriendsFragments;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import sg.edu.np.MulaSave.HomePage.FriendsActivity;
import sg.edu.np.MulaSave.R;
import sg.edu.np.MulaSave.User;

public class FriendsFragment extends Fragment {

    RecyclerView friendRecycler;
    DatabaseReference databaseRefUser = FirebaseDatabase
            .getInstance("https://mad-ay22-p05-team-b-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("user");
    FirebaseUser usr = FirebaseAuth.getInstance().getCurrentUser();
    ArrayList<User> friendList;
    SearchView searchFriendList;
    TextView friendNoDisplay;

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        friendRecycler = view.findViewById(R.id.friendRecycler);
        friendList = new ArrayList<>();
        friendNoDisplay = view.findViewById(R.id.friendNoDisplay);
        searchFriendList = view.findViewById(R.id.searchFriendList);

        ViewFriendAdapter fAdapter = new ViewFriendAdapter(friendList,1);//1 means friend list
        initData(friendList, fAdapter, "friends",friendNoDisplay);//initialise data

        LinearLayoutManager vLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);//set layout, 1 item per row
        friendRecycler.setLayoutManager(vLayoutManager);
        friendRecycler.setItemAnimator(new DefaultItemAnimator());
        friendRecycler.setAdapter(fAdapter);//set adapter

        searchFriendList.setSubmitButtonEnabled(true);//enable submit button
        searchFriendList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchFriendList.setIconified(false);//make the whole searchview available for input
            }
        });
        //layout setting for the searchview
        searchFriendList.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchOpen(getActivity(),getActivity().findViewById(R.id.friendListConstraint),R.id.friendListConstraint, R.id.searchFriendsCard);//format on search click
                filterDataBySearch(searchFriendList, friendList, fAdapter,"friends");
            }
        });//end of onsearchclick listener

        searchFriendList.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchClose(getActivity(), getActivity().findViewById(R.id.friendListConstraint), R.id.friendListConstraint, R.id.searchFriendsCard);//format on search close
                return false;//return false so that icon closes back on close
            }
        });
    }
    public static void searchOpen(Context context, View constraintView, int layoutView, int searchViewCard){
        ConstraintLayout layout = (ConstraintLayout) constraintView;
        ConstraintSet set = new ConstraintSet();
        set.clone(layout);

        //set constraints for start and end
        set.connect(searchViewCard,ConstraintSet.START, layoutView,ConstraintSet.START,0);
        set.connect(searchViewCard,ConstraintSet.END, layoutView,ConstraintSet.END,0);
        set.applyTo(layout);
    }

    //function to set searchview on search close
    public static void searchClose(Context context, View constraintView, int layoutView, int searchViewCard){
        //to convert margin to dp
        Resources r = context.getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                24,
                r.getDisplayMetrics()
        );
        //set layout
        ConstraintLayout layout = (ConstraintLayout) constraintView;
        ConstraintSet set = new ConstraintSet();
        set.clone(layout);
        //clear constraints
        set.clear(searchViewCard, ConstraintSet.START);
        set.connect(searchViewCard, ConstraintSet.END, layoutView, ConstraintSet.END,px);
        set.applyTo(layout);
    }

    public static void initData(ArrayList<User> friendList, ViewFriendAdapter adapter, String path, TextView view){
        DatabaseReference databaseRefUser = FirebaseDatabase
                .getInstance("https://mad-ay22-p05-team-b-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("user");
        FirebaseUser usr = FirebaseAuth.getInstance().getCurrentUser();

        if(path.equals("explore")){//special method for explore page as explore gets data differently
            databaseRefUser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {//get data on success
                    friendList.clear();
                    for (DataSnapshot ss : snapshot.getChildren()){
                        User user = new User();
                        for (DataSnapshot ds : ss.getChildren()){//because the users may have wishlists and other fields, cannot extract directly to user class
                            if (ds.getKey().equals("uid")){user.setUid(ds.getValue().toString());}
                            if(ds.getKey().equals("email")){user.setEmail(ds.getValue().toString());}
                            if(ds.getKey().equals("username")){user.setUsername(ds.getValue().toString());}
                        }
                        if (!user.getUid().equals(usr.getUid())){
                            friendList.add(user);//add user to the list
                        }
                    }
                    if (friendList.size() != 0) {
                        view.setVisibility(View.INVISIBLE);
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        else {
            databaseRefUser.child(usr.getUid()).child(path).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    friendList.clear();
                    for (DataSnapshot ss : snapshot.getChildren()) {//ss.getKey() is the uid of each friend
                        databaseRefUser.child(ss.getKey().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User user = new User();
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    if (ds.getKey().equals("uid")) {
                                        user.setUid(ds.getValue().toString());
                                    }
                                    if (ds.getKey().equals("email")) {
                                        user.setEmail(ds.getValue().toString());
                                    }
                                    if (ds.getKey().equals("username")) {
                                        user.setUsername(ds.getValue().toString());
                                    }
                                }
                                if (FriendsActivity.addNewUser(user, friendList)) {
                                    friendList.add(user);
                                    if (friendList.size() != 0) {
                                        view.setVisibility(View.INVISIBLE);
                                    }
                                    adapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    //filter fragment rrecyclerviews by user input
    public static void filterDataBySearch(SearchView searchFriends, ArrayList<User> friendList, ViewFriendAdapter adapter,String path){
        DatabaseReference databaseRefUser = FirebaseDatabase
                .getInstance("https://mad-ay22-p05-team-b-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("user");
        FirebaseUser usr = FirebaseAuth.getInstance().getCurrentUser();

        searchFriends.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                friendList.clear();
                if (path.equals("explore")) {//the way to get users from explore is quite different
                    databaseRefUser.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {//get data on success
                            friendList.clear();
                            for (DataSnapshot ss : snapshot.getChildren()){
                                //User extractUser = ss.getValue(User.class);
                                User user = new User();
                                for (DataSnapshot ds : ss.getChildren()){//because the users may have wishlists and other fields, cannot extract directly to user class
                                    if (ds.getKey().equals("uid")){user.setUid(ds.getValue().toString());}
                                    if (ds.getKey().equals("email")){user.setEmail(ds.getValue().toString());}
                                    if (ds.getKey().equals("username")){ user.setUsername(ds.getValue().toString());}
                                }
                                if (!user.getUid().equals(usr.getUid())){//dont show the user himself
                                    if (user.getUsername().toLowerCase().contains(s.toLowerCase())) {
                                        friendList.add(user);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                else {//get data from friendlist or request list
                    databaseRefUser.child(usr.getUid()).child(path).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ss : snapshot.getChildren()) {//ss.getKey() is the uid of each friend
                                databaseRefUser.child(ss.getKey().toString()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        User user = new User();
                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                            if (ds.getKey().equals("uid")) {user.setUid(ds.getValue().toString());}
                                            if (ds.getKey().equals("email")) {user.setEmail(ds.getValue().toString());}
                                            if (ds.getKey().equals("username")) {user.setUsername(ds.getValue().toString());}
                                        }
                                        //add the user if the username is under the
                                        if (user.getUsername().toLowerCase().contains(s.toLowerCase())) {//dont add if the input is none
                                            friendList.add(user);
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                    return false;
                }

        });
    }//end of filter data by search
}