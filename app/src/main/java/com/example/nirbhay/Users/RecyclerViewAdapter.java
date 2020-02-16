package com.example.nirbhay.Users;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirbhay.R;

import java.text.SimpleDateFormat;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * It is an adapter that binds recycler view to display the posts.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    private List<PostDetails> posts;
    private final ClickHandler clickHandler;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        //connects views in row_layout
        public CircleImageView viewProfile;
        public TextView ownerName, dateTime, postBody, upVoteCount, downVoteCount;
        public Button upvote,downvote;
        private ClickHandler clickHandler;
        public ViewHolder(View view){
            super(view);
            viewProfile = (CircleImageView) view.findViewById(R.id.viewProfile);

            upVoteCount = (TextView) view.findViewById(R.id.upVoteCount);
            downVoteCount = (TextView) view.findViewById(R.id.downVoteCount);
            ownerName = (TextView)view.findViewById(R.id.ownerName);
            dateTime = (TextView) view.findViewById(R.id.dateTime);
            postBody = (TextView) view.findViewById(R.id.postBody);
            upvote = (Button) view.findViewById(R.id.upvote);
            downvote = (Button) view.findViewById(R.id.downvote);

            upvote.setOnClickListener(this);
            downvote.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(clickHandler!=null)
                clickHandler.onButtonClicked(view, getAdapterPosition());
        }
    }

    public RecyclerViewAdapter(List<PostDetails> posts, ClickHandler clickHandler){
        this.posts = posts;
        this.clickHandler = clickHandler;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_layout, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //set values for view in row_layout
        holder.clickHandler = this.clickHandler;
        PostDetails postDetails = posts.get(position);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/mm/yyyy hh:mm:ss");
        String date = simpleDateFormat.format(postDetails.getDate());
        holder.upVoteCount.setText(Long.toString(postDetails.getUpVoteCount()));
        holder.ownerName.setText(postDetails.getOwnerName());
        holder.downVoteCount.setText(Long.toBinaryString(postDetails.getDownVoteCount()));
        holder.dateTime.setText(date);
        holder.postBody.setText(postDetails.getPostBody());
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public interface ClickHandler {
        void onButtonClicked(View view, final int position);
    }
}

