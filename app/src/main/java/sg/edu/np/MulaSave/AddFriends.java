package sg.edu.np.MulaSave;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import sg.edu.np.MulaSave.FriendsFragments.ExploreFragment;
import sg.edu.np.MulaSave.FriendsFragments.FriendsFragment;
import sg.edu.np.MulaSave.FriendsFragments.FriendsActivityAdapter;
import sg.edu.np.MulaSave.FriendsFragments.RequestsFragment;

public class AddFriends extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);

        tabLayout = findViewById(R.id.tabLayoutFriends);
        viewPager = findViewById(R.id.viewPagerFriends);

        tabLayout.setupWithViewPager(viewPager);

        FriendsActivityAdapter adapter = new FriendsActivityAdapter(getSupportFragmentManager());
        adapter.addFragment(new FriendsFragment(), "Friends");
        adapter.addFragment(new RequestsFragment(), "Requests");
        adapter.addFragment(new ExploreFragment(), "Explore");
        viewPager.setAdapter(adapter);

    }
}