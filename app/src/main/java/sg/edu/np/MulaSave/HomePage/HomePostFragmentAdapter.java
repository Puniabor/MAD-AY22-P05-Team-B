package sg.edu.np.MulaSave.HomePage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Adapter for the explore and friends tabs fragment under the home fragment
 */
public class HomePostFragmentAdapter extends FragmentPagerAdapter {
    private final ArrayList<Fragment> hnFragArrayList = new ArrayList<>();
    private final ArrayList<String> hnFragTitle = new ArrayList<>();
    public HomePostFragmentAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return hnFragArrayList.get(position);
    }

    @Override
    public int getCount() {
        return hnFragArrayList.size();
    }

    public void addFragment(Fragment fragment, String title){
        hnFragArrayList.add(fragment);
        hnFragTitle.add(title);
    }
    @Override
    public int getItemPosition(@NonNull Object object) {
        return super.getItemPosition(object);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position){
        return hnFragTitle.get(position);
    }
}
