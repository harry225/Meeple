package com.meeple.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.meeple.R;
import com.meeple.json.ConversationObject;
import com.meeple.utils.Constant;
import com.meeple.utils.Debug;
import com.meeple.utils.Utils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

/**
 * Created by harry on 9/28/15.
 */
public class ChatArrayAdapter extends BaseAdapter {


    private ArrayList<ConversationObject> data = new ArrayList();

    Activity context;
    private static final int TYPE_LEFT = 0;
    private static final int TYPE_RIGHT = 1;
    private static final int TYPE_MAX_COUNT = 2;

    public ChatArrayAdapter(Activity activity) {
        this.context = activity;
    }

    public void add(ConversationObject object) {
        if (object != null)
            data.add(object);
        Debug.e("data size is", data.size() + "-");
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<ConversationObject> list) {

        if (list != null) {
            data.clear();
            data.addAll(list);
        }
        notifyDataSetChanged();

    }

    @Override
    public int getItemViewType(int position) {
        ConversationObject chatMessageObj = data.get(position);
        if (StringUtils.isEmpty(chatMessageObj.fromUserId)) {
            return TYPE_RIGHT;
        } else {
            if (chatMessageObj.fromUserId.equals(Utils.getFromUserDefaults(context, Constant.PARAMS_ID))) {
                return TYPE_RIGHT;
            } else {
                return TYPE_LEFT;
            }
        }
    }

    @Override
    public int getViewTypeCount() {

        return TYPE_MAX_COUNT;

    }

    public int getCount() {

        return this.data.size();

    }

    public ConversationObject getItem(int index) {

        return (ConversationObject) this.data.get(index);

    }

    @Override
    public long getItemId(int i) {

        return 0;

    }

    public View getView(int position, View convertView, ViewGroup parent) {


        ConversationObject chatMessageObj = (ConversationObject) getItem(position);
        ViewHolder holder = null;
        int type = getItemViewType(position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            holder = new ViewHolder();

            switch (type) {


                case TYPE_LEFT:
                    convertView = inflater.inflate(R.layout.list_chat_left, parent, false);
                    break;

                case TYPE_RIGHT:
                    convertView = inflater.inflate(R.layout.list_chat_right, parent, false);
                    break;

                default:
                    convertView = inflater.inflate(R.layout.list_chat_right, parent, false);


            }

            holder.llchatContainer = (LinearLayout) convertView.findViewById(R.id.llchatContainer);
            holder.tvchatMessage = (TextView) convertView.findViewById(R.id.tvchatMessage);
            holder.tvChatTime = (TextView) convertView.findViewById(R.id.tvChatTime);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvchatMessage.setText(chatMessageObj.message);

        if (data.get(position).islocally) {

            holder.tvChatTime.setText(data.get(position).createdAt);

        } else {
            holder.tvChatTime.setText(Utils.getDateinWords(chatMessageObj.createdAt));
        }


        return convertView;
    }

    public static class ViewHolder {
        public TextView tvchatMessage, tvChatTime;
        public LinearLayout llchatContainer;
    }
}