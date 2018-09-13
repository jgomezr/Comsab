package com.comsab;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by julian_dev on 2/18/2018.
 */

public class MaterialAdapter extends RecyclerView.Adapter<MaterialAdapter.ViewHolder> {
    private static final String DEBUG_TAG ="MaterialAdapter";
    public Context context;
    public ArrayList<Card> cardList;

    public MaterialAdapter(Context context, ArrayList<Card> cardsList){
        this.context = context;
        this.cardList = cardsList;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        String name = cardList.get(position).getName();
        String location = cardList.get(position).getLocation();
        int color = cardList.get(position).getColorResource();
        TextView initial = viewHolder.initial;
        TextView nameTextView = viewHolder.name;
        nameTextView.setText(name);
        initial.setBackgroundColor(color);
        initial.setText(Character.toString(name.charAt(0)));
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder viewHolder) {
        super.onViewDetachedFromWindow(viewHolder);
        viewHolder.itemView.clearAnimation();
    }

    @Override
    public void onViewAttachedToWindow(ViewHolder viewHolder) {
        super.onViewAttachedToWindow(viewHolder);
        //animateCircularReveal(viewHolder.itemView);
    }

    //public void animateCircularReveal(View view) {
       // int centerX = 0;
        //int centerY = 0;
       // int startRadius = 0;
       // int endRadius = Math.max(view.getWidth(), view.getHeight());
       // Animator animation = ViewAnimationUtils.createCircularReveal(view, centerX, centerY, startRadius, endRadius);
        //view.setVisibility(View.VISIBLE);
        //animation.start();
    //}



    public void addCard(String name,String location, int color) {
        Card card = new Card();
        card.setName(name);
        card.setLocation(location);
        card.setColorResource(color);
        card.setId(getItemCount());
        cardList.add(card);
       // ((MainActivity) context).doSmoothScroll(getItemCount());
        notifyItemInserted(getItemCount());
    }

    public void updateCard(String name, int list_position) {
        cardList.get(list_position).setName(name);
        Log.d(DEBUG_TAG, "list_position is " + list_position);
        notifyItemChanged(list_position);
    }

    @Override
    public int getItemCount() {
        if (cardList.isEmpty()) {
            return 0;
        } else {
            return cardList.size();
        }
    }

    @Override
    public long getItemId(int position) {
        return cardList.get(position).getId();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater li = LayoutInflater.from(viewGroup.getContext());
        View v = li.inflate(R.layout.card_view_holder, viewGroup, false);
        return new ViewHolder(v);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView initial;
        private TextView name;

        public ViewHolder(View v) {
            super(v);
            initial = (TextView) v.findViewById(R.id.initial);
            name = (TextView) v.findViewById(R.id.name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("RestrictedApi")
                @Override
                public void onClick(View v) {
                    Pair<View, String> p1 = Pair.create((View) initial, MainActivity.TRANSITION_INITIAL);
                    Pair<View, String> p2 = Pair.create((View) name, MainActivity.TRANSITION_NAME);

                    ActivityOptionsCompat options;
                    Activity act = (AppCompatActivity) context;
                    options = ActivityOptionsCompat.makeSceneTransitionAnimation(act, p1, p2);

                    int requestCode = getAdapterPosition();
                    String name = cardList.get(requestCode).getName();
                    String location = cardList.get(requestCode).getLocation();
                    int color = cardList.get(requestCode).getColorResource();

                    Log.d(DEBUG_TAG, "SampleMaterialAdapter itemView listener for Edit adapter position " + requestCode);

                    Intent transitionIntent = new Intent(context, PrintActivity.class);
                    transitionIntent.putExtra(MainActivity.EXTRA_NAME, location);
                    transitionIntent.putExtra(MainActivity.EXTRA_INITIAL, Character.toString(name.charAt(0)));
                    transitionIntent.putExtra(MainActivity.EXTRA_COLOR, color);
                    transitionIntent.putExtra(MainActivity.EXTRA_UPDATE, false);
                    ((AppCompatActivity) context).startActivityForResult(transitionIntent, requestCode, options.toBundle());
                }
            });
        }
    }
}
