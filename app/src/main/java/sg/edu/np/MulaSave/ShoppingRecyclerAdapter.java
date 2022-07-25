package sg.edu.np.MulaSave;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;


public class ShoppingRecyclerAdapter extends RecyclerView.Adapter<ShoppingViewHolder> {
    //adapter shared by shopping, wishlist and uploads
    private ArrayList<Product> data;

    DatabaseReference databaseRefUser = FirebaseDatabase
            .getInstance("https://mad-ay22-p05-team-b-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("user");

    DatabaseReference databaseRefProduct = FirebaseDatabase
            .getInstance("https://mad-ay22-p05-team-b-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("product");

    LayoutInflater inflater;
    int layoutType; //toggle between search product view and shopping list view
    // 1 = shopping search product view
    // 2 = wishlist view
    FirebaseUser usr = FirebaseAuth.getInstance().getCurrentUser();

    public ShoppingRecyclerAdapter(ArrayList<Product> input, Context  context, int _layoutType) {
        this.data = input;
        this.inflater = LayoutInflater.from(context);
        this.layoutType = _layoutType;
    }

    @Override
    public int getItemViewType(final int position) {
        if (this.layoutType == 1){
            return 1;//shopping search product view
        }
        else{
            return 2;//wishlist view
        }
    }//

    @Override
    public ShoppingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if(viewType == 1){//search product view
            view = inflater.inflate(R.layout.shopping_view_holder,parent,false);
        }
        else{//wishlist view
            view = inflater.inflate(R.layout.wishlist_row,parent,false);
        }
//        else if(viewType == 3 ){//community upload view
//
//        }
        return new ShoppingViewHolder(view,viewType);
    }

    @Override
    public void onBindViewHolder(ShoppingViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Product p = data.get(position);

        holder.productTitle.setText(p.getTitle());
        String price = "0.0";
        if (p.getPrice()!=null){
            price = String.format("$%.2f",p.getPrice());
        }



// To set the notify users if the product is reserved, sold or available
        databaseRefUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean isreserved = false;
                Boolean isSold = false;
                for(DataSnapshot ds: snapshot.getChildren()){
                    for (DataSnapshot ds1: ds.child("Reserve").getChildren()){
                        Product dbprod = ds1.getValue(Product.class);
                        if (dbprod.getAsin().equals(p.getAsin())){
                            isreserved = true;
                        }
                    }
                    for (DataSnapshot ds1: ds.child("Sold").getChildren()){
                        Product dbprod = ds1.getValue(Product.class);
                        if (dbprod.getAsin().equals(p.getAsin())){
                            isSold = true;
                        }
                }
                if (isreserved) {
                    holder.statusProduct.setText("Reserved");
                    holder.statusProduct.setTextColor(Color.parseColor("#FFF3BA2B"));
                }
                if (isSold){
                    holder.statusProduct.setText("Sold");
                    holder.statusProduct.setTextColor(Color.parseColor("#FFE40846"));
                }
                else{

                }
            }
        }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(holder.getItemViewType() == 1){
            holder.seepaymentBtn.setVisibility(View.INVISIBLE);
            if (p.getSellerUid().equals(usr.getUid().toString())){
                holder.seepaymentBtn.setVisibility(View.VISIBLE);// set visible if current user is creator
            }
        }

        if(holder.getItemViewType() == 1){
            holder.seepaymentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(holder.seepaymentBtn.getContext(), SellerPaymentView.class);
                    intent.putExtra("product", p);
                    holder.seepaymentBtn.getContext().startActivity(intent);
                }
            });

            holder.prodRemove.setVisibility(View.INVISIBLE);
            if(p.getSellerUid().equals(usr.getUid().toString())){
                holder.prodRemove.setVisibility(View.VISIBLE);//set visible if current user is creator
                holder.prodRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.prodRemove.getContext());
                        View v = LayoutInflater.from(holder.prodRemove.getContext()).inflate(R.layout.upload_delete_dialog, null, false);
                        builder.setView(v);
                        final AlertDialog alertDialog = builder.create();
                        TextView noRemoveUpload = v.findViewById(R.id.noRemoveUpload);
                        TextView confirmRemoveUpload = v.findViewById(R.id.confirmRemoveUpload);
                        noRemoveUpload.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                alertDialog.dismiss();
                            }
                        });
                        confirmRemoveUpload.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                databaseRefUser.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for(DataSnapshot ds: snapshot.getChildren()){
                                            for (DataSnapshot ds1: ds.child("Reserve").getChildren()){
                                                Product prod = ds1.getValue(Product.class);
                                                if (p.getImageUrl().equals(prod.getImageUrl())){
                                                    ds1.getRef().removeValue();
                                                }
                                            }
                                            for (DataSnapshot ds1: ds.child("Sold").getChildren()){
                                                Product prod = ds1.getValue(Product.class);
                                                if (p.getImageUrl().equals(prod.getImageUrl())){
                                                    ds1.getRef().removeValue();
                                                }
                                            }
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                databaseRefProduct.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for(DataSnapshot ds: snapshot.getChildren()){
                                            Product prod = ds.getValue(Product.class);
                                            if(prod.getAsin().equals(p.getAsin())){
                                                ds.getRef().removeValue();
                                                ShoppingRecyclerAdapter.this.notifyItemRemoved(position);
                                            }
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                alertDialog.dismiss();
                            }
                        });
                        alertDialog.show();
                    }
                });
            }
        }

        holder.productPrice.setText(price);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(holder.productListing);//close the constraint layout
        if(p.getRating()!=null){
            if(Math.signum(p.getRating()) == 0){//check if rating is 0 (rating is not available)
                holder.prodRating.setVisibility(View.GONE);//rating bar gone
            }
            else {//rating is available (set rating)
                holder.prodRating.setVisibility(View.VISIBLE);//rating bar visible
                holder.prodRating.setRating(p.getRating());
            }
        }


        holder.productWebsite.setText(p.getWebsite());

        Picasso.get()
                .load(p.getImageUrl())
                .into(holder.productImage);

        holder.productListing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //dialog box
                showProductDialog(holder.productListing.getContext(), p);
            }
        });
        //use title, imageurl and website name as unique id for the listing - replacing all characters except for alphabets and numbers

        DatabaseReference databaseRefUser = FirebaseDatabase
                .getInstance("https://mad-ay22-p05-team-b-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("user");
        FirebaseUser usr = FirebaseAuth.getInstance().getCurrentUser();

        Log.d("ProductNullCheck"," "+position+" "+p.getTitle());
        String wishlistUnique = (p.getTitle() + (p.getImageUrl().substring(p.getImageUrl().length()-15))+ p.getWebsite()).replaceAll("[^a-zA-Z0-9]", "");

        databaseRefUser.child(usr.getUid().toString()).child("wishlist").addListenerForSingleValueEvent(new ValueEventListener() {//access users wishlist
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(wishlistUnique)){
                    holder.prodFavourite.setColorFilter(ContextCompat.getColor(holder.prodFavourite.getContext(), R.color.custom_red));//use custom red color if product is in wishlist
                }
                else{
                    holder.prodFavourite.setColorFilter(ContextCompat.getColor(holder.prodFavourite.getContext(), R.color.custom_gray));//use custom gray color if product is not in wishlist
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("DatabaseError", String.valueOf(error));
            }
        });

        holder.prodFavourite.setOnClickListener(new View.OnClickListener() {//on click listener for favourite button in searching of product
            @Override
            public void onClick(View view) {
                //Reads user wishlist to check if item has already been liked/added to wishlist
                databaseRefUser.child(usr.getUid().toString()).child("wishlist").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if(task.getResult().hasChild(wishlistUnique)){

                            //custom dialog for removing of wishlist item
                            AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                            View v = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.remove_wislist,null, false);
                            builder.setView(v);
                            ImageView pic = v.findViewById(R.id.wishlistPic);
                            Uri newUri = Uri.parse(p.getImageUrl());
                            Picasso.get().load(newUri).resize(200,200).into(pic);
                            final AlertDialog alertDialog = builder.create();

                            //positive button (remove item)
                            v.findViewById(R.id.dPositiveText).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    databaseRefUser.child(usr.getUid().toString()).child("wishlist").child(wishlistUnique).removeValue();
                                    holder.prodFavourite.setColorFilter(ContextCompat.getColor(holder.prodFavourite.getContext(), R.color.custom_gray));//use custom gray color
                                    if(holder.remove == true){//remove, because the wishlist is using the adapter
                                        data.clear();
                                    }//but if shopping or upload fragments are using, do not remove the item from the recyclerview
                                    else{//notify if is shopping or community view
                                        ShoppingRecyclerAdapter.this.notifyDataSetChanged();
                                    }
                                    alertDialog.dismiss();
                                }
                            });
                            //negative button (cancel removal)
                            v.findViewById(R.id.dNegativeText).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    alertDialog.dismiss();
                                }
                            });

                            //remove the extra parts outside of the cardview
                            if (alertDialog.getWindow() != null){
                                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable());
                            }
                            alertDialog.show();
                        }
                        else{
                            databaseRefUser.child(usr.getUid().toString()).child("wishlist").child(wishlistUnique).setValue(p);//add product if the product does not exist in the database
                            holder.prodFavourite.setColorFilter(ContextCompat.getColor(holder.prodFavourite.getContext(), R.color.custom_red));//use custom red color
                            addNotifications(usr.getUid(), p.getSellerUid(), p.getAsin());
                        }
                        notifyDataSetChanged();
                    }
                });

                // showing favourite
                holder.prodFavourite.setColorFilter(ContextCompat.getColor(holder.prodFavourite.getContext(), R.color.custom_red));//use custom red color
            }
        });
    }//end of onBindViewHolder

    @Override
    public int getItemCount() {
        return data.size();
    }

    private void addNotifications(String buyerid, String sellerid, String productid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("notifications").child(sellerid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", buyerid);
        hashMap.put("text", "liked product");
        hashMap.put("productid", productid);
        hashMap.put("ispost",true);

        reference.push().setValue(hashMap);
    }

    //custom product dialog information message
    private void showProductDialog(Context context,Product p) {
        //String picUri, Double price, String website, String link
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.product_dialog,null,false);
        builder.setView(view);
        //load information
        ImageView pic = view.findViewById(R.id.dialogPic);
        Uri picU = Uri.parse(p.getImageUrl());
        Picasso.get().load(picU).resize(200,200).into(pic);
        ((TextView) view.findViewById(R.id.dialogPrice)).setText(String.format("$ %.2f",p.getPrice()));
        ((TextView) view.findViewById(R.id.dialogWebsite)).setText(p.getWebsite());

        final AlertDialog alertDialog = builder.create();
        //open browser
        view.findViewById(R.id.dialogOpen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (p.getLink().equals("link")){//products from community uploads have string link as the link var
                    Intent i = new Intent(context, descriptionpage.class);
                    i.putExtra("product",p);//pass product into desc
                    context.startActivity(i);//start the product desc activity
                }
                else{//start browser intent
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(p.getLink()));
                    context.startActivity(browserIntent);//start browser
                }
                alertDialog.dismiss();
            }
        });

        //close browser
        view.findViewById(R.id.dialogClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();//close
            }
        });

        //remove the extra parts outside of the cardview
        if (alertDialog.getWindow() != null){
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable());
        }
        alertDialog.show();
    }
}


