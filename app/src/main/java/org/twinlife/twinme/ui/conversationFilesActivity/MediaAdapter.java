package org.twinlife.twinme.ui.conversationFilesActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.ui.baseItemActivity.Item;

public class MediaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "MediaAdapter";
    private static final boolean DEBUG = false;

    private final ConversationFilesActivity mConversationFilesActivity;

    private UIFileSection mFileSection;

    public MediaAdapter(ConversationFilesActivity listActivity) {

        mConversationFilesActivity = listActivity;
        setHasStableIds(true);
    }

    public void setFileSection(UIFileSection fileSection) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setFileSection: " + fileSection);
        }

        mFileSection = fileSection;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemId: position=" + position);
        }

        Item item = mFileSection.getItems().get(position);
        return item.getItemId();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mConversationFilesActivity.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.conversation_files_activity_media_item, parent, false);

        MediaViewHolder mediaViewHolder = new MediaViewHolder(convertView);
        convertView.setOnClickListener(v -> {
            int position = mediaViewHolder.getBindingAdapterPosition();
            if (position >= 0) {
                mConversationFilesActivity.onItemClick(mFileSection.getItems().get(position));
            }
        });

        return mediaViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        MediaViewHolder mediaViewHolder = (MediaViewHolder) viewHolder;
        mediaViewHolder.onBind(mFileSection.getItems().get(position), mConversationFilesActivity);
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        if (mFileSection == null) {
            return 0;
        }

        return mFileSection.getCount();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

        int position = viewHolder.getBindingAdapterPosition();
        if (position != -1) {
            MediaViewHolder mediaViewHolder = (MediaViewHolder) viewHolder;
            mediaViewHolder.onViewRecycled();
            mediaViewHolder.onBind(mFileSection.getItems().get(position), mConversationFilesActivity);
        }
    }
}