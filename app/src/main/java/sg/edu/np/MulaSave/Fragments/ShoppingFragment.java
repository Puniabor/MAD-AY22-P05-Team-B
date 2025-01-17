package sg.edu.np.MulaSave.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import sg.edu.np.MulaSave.APIHandler;
import sg.edu.np.MulaSave.BuildConfig;
import sg.edu.np.MulaSave.MainActivity;
import sg.edu.np.MulaSave.Product;
import sg.edu.np.MulaSave.R;
import sg.edu.np.MulaSave.ShoppingRecyclerAdapter;
import sg.edu.np.MulaSave.wishlistFilterAdapter;

public class ShoppingFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressDialog progressDialog;
    RecyclerView recyclerViewFilter;
    private ArrayList<Product> productList = MainActivity.productList;//Take previously loaded productList
    private String query = MainActivity.query;

    public ShoppingFragment() {
    }

    public static ShoppingFragment newInstance() {
        ShoppingFragment fragment = new ShoppingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shopping, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Intent to shopping search fragments which is another page for suggestions
        ImageView searchBtn = view.findViewById(R.id.shoppingSearchImageBtn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShoppingSearchFragment nextFrag= new ShoppingSearchFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frameLayout, nextFrag, "findThisFragment")
                        .commit();
            }
        });
//        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
//        //Associate the searchable configuration with the SearchView
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
//        //Lets users click on the submit button rather than "Enter" or "Return" on their keyboards
//        searchView.setSubmitButtonEnabled(true);
//        //Enables query refinement from search suggestions
//        searchView.setQueryRefinementEnabled(true);

//        int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
//
//        EditText searchEdit = searchView.findViewById(id);
//        searchEdit.setTextColor(Color.BLACK);
//
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("Recent API queries", Context.MODE_PRIVATE);

        recyclerView = view.findViewById(R.id.shoppingrecyclerview);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1,GridLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(gridLayoutManager);

        if (query!=null){//If suggestion has been clicked and intented, process the query which has been clicked(hence query will not be null)
            productList=null;//Clear previously loaded products as a new one will be loaded based on suggestion clicked
            List<Product> arrayItems;
            String serializedObject = sharedPreferences.getString(query, null);//Extract previously queried product list from SharedPreferences
            if (serializedObject != null) {//If list of previous query was saved into SharedPreferences, extract it and use it in recycler (This is to reduce API requests made)
                ((TextView)getView().findViewById(R.id.placeholderText)).setVisibility(View.GONE);//hide the placeholder text
                ((ImageView)getView().findViewById(R.id.shoppingMulasaveLogo)).setVisibility(View.GONE);//hide the mulasave logo background
                Gson gson = new Gson();
                Type type = new TypeToken<List<Product>>() {}.getType();
                arrayItems = gson.fromJson(serializedObject, type);//Convert from Json to List<Product>

                ShoppingRecyclerAdapter pAdapter = new ShoppingRecyclerAdapter((ArrayList<Product>) arrayItems, getContext(),2);
                //WishList Filters
                recyclerViewFilter = view.findViewById(R.id.shoppingFilter);
                wishlistFilterAdapter wFilterAdapter = new wishlistFilterAdapter(view,pAdapter,(ArrayList<Product>) arrayItems,3);
                //Layout manager for filters recyclerview
                LinearLayoutManager hLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);//set horizontal layout
                recyclerViewFilter.setLayoutManager(hLayoutManager);
                recyclerViewFilter.setItemAnimator(new DefaultItemAnimator());
                recyclerViewFilter.setAdapter(wFilterAdapter);//set adapter for wishlist filters
                recyclerView.setAdapter(pAdapter);

                MainActivity.productList = (ArrayList<Product>) arrayItems;//Set new product list as currently focused list (makes product list persistent in case user switches fragments)
            }else{//If no list can be found in SharedPreferences, load the query again from API
                ((TextView)getView().findViewById(R.id.placeholderText)).setVisibility(View.GONE);//hide the placeholder text
                ((ImageView)getView().findViewById(R.id.shoppingMulasaveLogo)).setVisibility(View.GONE);//hide the mulasave logo background
                //Toast.makeText(getContext(),"No previous searches has been saved. Try searching instead.",Toast.LENGTH_LONG);
                productList = new ArrayList<Product>();//Create new list to clear previously loaded products for new query
                new getProducts(query,productList,view).execute();

            }
        }
        if (productList!=null) {//Checks for previously loaded productList to display(i.e. the user clicked another fragment and returned back)
            if(productList.size()!=0){
                ((TextView)getView().findViewById(R.id.placeholderText)).setVisibility(View.GONE);//hide the placeholder text
                ((ImageView)getView().findViewById(R.id.shoppingMulasaveLogo)).setVisibility(View.GONE);//hide the mulasave logo background
                ShoppingRecyclerAdapter pAdapter = new ShoppingRecyclerAdapter(productList, getContext(),2);
                //WishList Filters
                recyclerViewFilter = view.findViewById(R.id.shoppingFilter);
                wishlistFilterAdapter wFilterAdapter = new wishlistFilterAdapter(view,pAdapter,productList,3);
                //Layout manager for filters recyclerview
                LinearLayoutManager hLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);//set horizontal layout
                recyclerViewFilter.setLayoutManager(hLayoutManager);
                recyclerViewFilter.setItemAnimator(new DefaultItemAnimator());
                recyclerViewFilter.setAdapter(wFilterAdapter);//set adapter for wishlist filters
                recyclerView.setAdapter(pAdapter);
            }
        }

//        searchView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                searchView.setIconified(false);//Displays entire search bar when clicked by user
//            }
//        });
//
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() { //Grab products from API whenever a query is submitted
//            @Override
//            public boolean onQueryTextSubmit(String s) {
//                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getContext(),
//                        ProductSuggestionProvider.AUTHORITY, ProductSuggestionProvider.MODE);
//                suggestions.saveRecentQuery(s, null); //Save query for search history suggestions in the future
//
//                productList = new ArrayList<Product>();//Create new list to clear previously loaded products for new query
//                new getProducts(s,productList,view).execute();//Gets products data from API
//                return true;
//            }
//            @Override
//            public boolean onQueryTextChange(String s) {
//                return false;
//            }
//        });
//
//        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
//            @Override
//            public boolean onSuggestionSelect(int i) {
//                return false;
//            }
//
//            @Override
//            public boolean onSuggestionClick(int i) {
//                Intent intent = new Intent();
//                intent.putExtra("shopping",1);
//                return false;
//            }
//        });
//
//        //set on searchview open listener for searchview
//        searchView.setOnSearchClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //((TextView)getView().findViewById(R.id.placeholderText)).setVisibility(View.GONE);//hide the placeholder text
//
//                ((TextView)getView().findViewById(R.id.shoppingTitle)).setVisibility(View.GONE);//set the title to be gone
//                ConstraintLayout layout = (ConstraintLayout) getView().findViewById(R.id.shoppingConstraintLayout);//get constraintlayout
//                ConstraintSet set = new ConstraintSet();
//                set.clone(layout);
//                //set constraints for the title and searchview
//                set.connect(R.id.shoppingSearchCard, ConstraintSet.START,R.id.shoppingConstraintLayout,ConstraintSet.START,0);
//                set.connect(R.id.shoppingSearchCard, ConstraintSet.END,R.id.shoppingConstraintLayout,ConstraintSet.END,0);
//                set.applyTo(layout);
//            }
//        });
//
//        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
//            @Override
//            public boolean onClose() {
//                ((TextView)getView().findViewById(R.id.shoppingTitle)).setVisibility(View.VISIBLE);
//
//                //to convert margin to dp
//                Resources r = getView().getResources();
//                int px = (int) TypedValue.applyDimension(
//                        TypedValue.COMPLEX_UNIT_DIP,
//                        24,
//                        r.getDisplayMetrics()
//                );
//
//                //set layout
//                ConstraintLayout layout = (ConstraintLayout) getView().findViewById(R.id.shoppingConstraintLayout);
//                ConstraintSet set = new ConstraintSet();
//                set.clone(layout);
//                //clear constraints
//                set.clear(R.id.shoppingSearchCard, ConstraintSet.START);
//                set.connect(R.id.shoppingSearchCard, ConstraintSet.END,R.id.shoppingConstraintLayout,ConstraintSet.END,px);
//                set.applyTo(layout);
//                return false;//return false so that icon closes back on close
//            }
//        });

        //to navigate user from homefrag to shoppingfrag
//        Bundle bundle = this.getArguments();
//        if(bundle!= null){
//            Boolean search = bundle.getBoolean("condition",false);
//            if(search){//if search == true, which means set searchview to active
//                searchView.performClick();
//                searchView.requestFocus();
//                //to show the keyboard
//                searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
//                    @Override
//                    public void onFocusChange(View view, boolean hasFocus) {
//                        if (hasFocus) {
//                            showInputMethod(view.findFocus());
//                        }
//                    }
//                });
//            }
//        }//end of bundle
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        try{//try catch because the product list may not be initialised
//            if (MainActivity.productList == null){//if there are items in the list
//                ((SearchView)getView().findViewById(R.id.searchFragQuery)).performClick();//click the searchview to show the previous state
//                ((SearchView)getView().findViewById(R.id.searchFragQuery)).setIconified(true);//onresume, hide the keyboard
//            }
//        }
//        catch (Exception e){
//            Log.e("error", "onResume: " + String.valueOf(e));
//        }
//    }

    private void showInputMethod(View view) {
        InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (mgr != null) {
            mgr.showSoftInput(view, 0);
        }
    }



    class getProducts extends AsyncTask<Void, Void, Void> {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("Recent API queries", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String query;
        ArrayList<Product> productList;
        View view;

        public getProducts(String _query, ArrayList<Product> pList,View v){
            this.query=_query;
            this.productList=pList;
            this.view=v;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            if(progressDialog.isShowing()){
                progressDialog.dismiss();
            }

            //Sorts list from lowest to highest price by default
            Collections.sort(productList,productPriceLowHigh);

            //Updates MainActivity's productList so that the list will not be destroyed alongside fragment and stays persistent
            MainActivity.productList = productList;

            //Display products with productList generated based on user's query
            ShoppingRecyclerAdapter pAdapter = new ShoppingRecyclerAdapter(productList, getContext(),2);
            //WishList Filters
            recyclerViewFilter = view.findViewById(R.id.shoppingFilter);
            wishlistFilterAdapter wFilterAdapter = new wishlistFilterAdapter(getView(),pAdapter,productList,3);

            //Layout manager for filters recyclerview
            LinearLayoutManager hLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);//set horizontal layout
            recyclerViewFilter.setLayoutManager(hLayoutManager);
            recyclerViewFilter.setItemAnimator(new DefaultItemAnimator());
            recyclerViewFilter.setAdapter(wFilterAdapter);//set adapter for wishlist filters
            recyclerView.setAdapter(pAdapter);

            //Save recently queried list into SharedPreferences
            setList(query,productList);

            //Set query back to null so that it will not be loaded again upon entering shopping fragment
            ShoppingFragment.this.query=null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("Please wait... (might take a minute!)");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Boolean demo = false;
            //Amazon API is skipped as our API has expired
            String[] apiList = new String[] {"walmart","amazon","ebay","target"};

            for(String website:apiList){//If it is a demo query, there is no need to iterate through all websites in apiList
                if(query.contains(website)){
                    String url = getAPIlink(query,website); //Finds URL link by processing through template based on online shopping site used, this is so the correct API & API keys are used
                    loadJsonfromUrl(url,website); //Loads json using URL query and extracts Product details from Json
                    demo=true;
                    break;
                }
            }
            if(!demo){//If it is not a demo, take product data from all APIs
                for (String i:apiList){
                    String url = getAPIlink(query,i); //Finds URL link by processing through template based on online shopping site used, this is so the correct API & API keys are used
                    if (url != "") { //Check if there is a readable URL(url can be empty string if non-demo Amazon link is asked for)
                        loadJsonfromUrl(url,i); //Loads json using URL query and extracts Product details from Json
                    }
                }
            }
            return null;
        }

        private String getAPIlink(String query, String website){
            String url = null;
            String apikey;
            query = query.replace(" ","+");

            if(query.toLowerCase().contains("walmart")){  //Allow use of demo APIs for demo purposes so that API requests are not wasted in testing
                url = "https://api.bluecartapi.com/request?api_key=demo&type=search&search_term=highlighter+pens&sort_by=best_seller";
            }
            else if(query.toLowerCase().contains("amazon")){
                url = "https://api.rainforestapi.com/request?api_key=demo&amazon_domain=amazon.com&type=search&search_term=memory+cards";
            }
            else if(query.toLowerCase().contains("ebay")){
                url = "https://api.countdownapi.com/request?api_key=demo&type=search&ebay_domain=ebay.com&search_term=memory+cards&sort_by=price_high_to_low";
            }
            else if(query.toLowerCase().contains("target")){
                url = "https://api.redcircleapi.com/request?api_key=demo&type=search&search_term=highlighter+pens&sort_by=best_seller";
            }
            else{
                if(website.toLowerCase().equals("amazon")){
                    //Actual search for products on Amazon is disabled as we have ran out of API requests
                    //apikey = BuildConfig.API_KEY_AMAZON;

                    //url = "https://api.rainforestapi.com/request?api_key="+apikey+"&type=search&amazon_domain=amazon.sg&search_term="+query;

                    //TEMPORARY DEMO API FOR TESTING
                    //url = "https://api.rainforestapi.com/request?api_key=demo&amazon_domain=amazon.com&type=search&search_term=memory+cards";
                    url="";
                }
                else if(website.toLowerCase().equals("walmart")){
                    apikey = BuildConfig.API_KEY_WALMART;
                    url = "https://api.bluecartapi.com/request?api_key="+apikey+"&type=search&search_term="+query+"&sort_by=best_seller";
                }
                else if(website.toLowerCase().equals("ebay")){
                    apikey = BuildConfig.API_KEY_EBAY;
                    url = "https://api.countdownapi.com/request?api_key="+apikey+"&type=search&ebay_domain=ebay.com&search_term="+query+"&sort_by=price_high_to_low";
                }
                else if(website.toLowerCase().equals("target")){
                    apikey = BuildConfig.API_KEY_TARGET;
                    url = "https://api.redcircleapi.com/request?api_key="+apikey+"&search_term="+query+"&type=search";
                }
                else{
                    Toast.makeText(getContext(),"We do not have APIs to that website yet!",Toast.LENGTH_SHORT).show();
                }
            }
            return url;
        }

        private void loadJsonfromUrl(String url, String website){
            APIHandler handler = new APIHandler();
            String jsonString = handler.httpServiceCall(url);//Loads API Json into a string
            Log.d("JSONInput",jsonString);//Check for success of pulling products from API and also number of requests left
            if (jsonString!=null){
                try {
                    JSONObject jsonObject = new JSONObject(jsonString);//Convert string to JSONObject to extract specific data
                    JSONArray products = jsonObject.getJSONArray("search_results");
                    for(int i=0;i<products.length();i++) {
                        JSONObject jsonObject1 = products.getJSONObject(i);

                        String title = "No title";
                        String image = "no image";
                        String link = "no link";
                        Double price = 0.0;
                        float ratingF = 0;

                        if (website.toLowerCase().equals("walmart")||website.toLowerCase().equals("target")) {//Product Json format of Walmart & Target API is different from Amazon & Ebay, so it is done separately
                            JSONObject productObject = jsonObject1.getJSONObject("product");
                            title = productObject.getString("title");
                            image = productObject.getString("main_image");
                            link = productObject.getString("link");

                            Double ratingD = productObject.optDouble("rating",0.0d);
                            ratingF = ratingD.floatValue();

                            JSONObject offersObject = jsonObject1.getJSONObject("offers").getJSONObject("primary");
                            price = offersObject.getDouble("price");
                        } else {
                            //String asin = jsonObject1.getString("asin"); //deleted because ebay API has no asin
                            title = jsonObject1.getString("title");
                            image = jsonObject1.getString("image");
                            link = jsonObject1.getString("link");
                            Double ratingD = jsonObject1.optDouble("rating",0.0d);
                            ratingF = ratingD.floatValue();

                            //JSONObject categoryObject = jsonObject1.getJSONArray("categories").getJSONObject(0); //deleted because ebay API does not have product categories
                            //String category = categoryObject.getString("name");

                            JSONObject priceObject = jsonObject1.getJSONObject("price");
                            price = priceObject.getDouble("value");
                        }

                        productList.add(new Product("asin", title, "category", price, image, link, ratingF, website,"desc","condition","meetup","sellerUid"));
                    }
                } catch (JSONException e) {
                    if(isAdded()){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Toast.makeText(getContext(),"Json Parsing Error",Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                                Log.i("error","Json Parsing Error");
                            }
                        });
                    }
                }
            }
            else{
                Toast.makeText(getContext(),"Server error",Toast.LENGTH_LONG).show();
                if(isAdded()){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(),"Server Error",Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }
        public <T> void setList(String key, List<T> list) {
            Gson gson = new Gson();
            String json = gson.toJson(list);//Converts list of products into json string, which can be saved into SharedPreferences
            set(key, json);
        }

        public void set(String key, String value) {//Stores string into SharedPreferences under a specified key for extraction later on
            editor.putString(key, value);
            editor.commit();
        }

        public Comparator<Product> productPriceLowHigh = new Comparator<Product>() {//Method to compare prices so that list can be sorted from low to high price
            @Override
            public int compare(Product p1, Product p2) {
                return Double.compare(p1.getPrice(),p2.getPrice());
            }
        };

    }
}