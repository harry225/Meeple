package com.meeple.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.meeple.R;
import com.meeple.activity.DashBoardActivity;
import com.meeple.json.BlockInfo;
import com.meeple.json.LoginInfo;
import com.meeple.json.UsersObject;
import com.meeple.utils.AlertMessages;
import com.meeple.utils.Constant;
import com.meeple.utils.Debug;
import com.meeple.utils.URLs;
import com.meeple.utils.Utils;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by harry on 9/28/15.
 */
public class UsersListAdapter extends BaseAdapter {

    public Activity mContext;
    AlertMessages alertMessages;
    ProgressDialog pd;
    boolean blockvisible;
    private LayoutInflater infalter;
    private ArrayList<UsersObject> data = new ArrayList<UsersObject>();
    int blockPosition;


    public UsersListAdapter(Activity c, boolean visibility) {

        this.mContext = c;
        this.blockvisible = visibility;
        infalter = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.alertMessages = new AlertMessages(mContext);

    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public UsersObject getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {

        final ViewHolder holder;
        if (convertView == null) {

            holder = new ViewHolder();

            convertView = infalter.inflate(R.layout.list_item_user, null);

            holder.tvUserName = (TextView) convertView
                    .findViewById(R.id.tvUserName);
            holder.tvBlock = (TextView) convertView.findViewById(R.id.tvBlock);
            holder.tvDistance = (TextView) convertView.findViewById(R.id.tvDistance);
            if (blockvisible) {
//                holder.tvDistance.setVisibility(View.VISIBLE);
            }
            convertView.setTag(holder);

        } else {

            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvUserName.setText(data.get(position).userID);
        if (data.get(position).isBlock == 0) {
            holder.tvBlock.setText("BLOCK");
        } else {
            holder.tvBlock.setText("UNBLOCK");
        }
        holder.tvBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.tvBlock.getText().toString().equalsIgnoreCase("BLOCK")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage("Are you sure you want to block " + data.get(position).userID + " ?");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            if (Utils.isNetworkAvailable(mContext)) {
                                blockPosition = position;
                                blockUser(position);

                            } else {
                                alertMessages.showErrornInConnection();
                            }
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                    alert.setCancelable(false);
                } else {
                    unBlockUser(position);
                }
            }
        });

        return convertView;
    }

    public class ViewHolder {

        TextView tvUserName, tvBlock, tvDistance;

    }

    public void addAll(ArrayList<UsersObject> files) {

        try {
            this.data.clear();
            this.data.addAll(files);

        } catch (Exception e) {
            e.printStackTrace();
        }

        notifyDataSetChanged();
    }

    public void blockUser(int position) {
        pd = null;
        pd = ProgressDialog.show(mContext, "", "Loading...", true,
                false);
        try {

            JSONObject json = new JSONObject();

            json.put("fromUserId", Utils.getFromUserDefaults(mContext, Constant.PARAMS_ID));
            json.put("toUserId", data.get(position).id);

            StringEntity stringEntity = new StringEntity(json.toString());
            stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(Constant.TIMEOUT);
            client.addHeader("Token", Utils.getFromUserDefaults(mContext, Constant.PARAMS_TOKEN));

            client.post(mContext, URLs.BLOCKUSER, stringEntity, "application/json", new BlockUserResponseHandler(position));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class BlockUserResponseHandler extends AsyncHttpResponseHandler {

        int position;

        public BlockUserResponseHandler(int position) {
            // TODO Auto-generated constructor stub
            this.position = position;
        }

        @Override
        public void onSuccess(int i, Header[] headers, byte[] responseBody) {

            pd.dismiss();

            String content = new String(responseBody);

            Debug.e("Block Resp", "-" + content);

            Gson gson = new Gson();
            Type type = new TypeToken<BlockInfo>() {
            }.getType();

            BlockInfo blockInfo = gson.fromJson(content, type);
            try {
                if (blockInfo.status_code == 1) {
                    data.get(position).isBlock = 1;
                    notifyDataSetChanged();
                } else {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//            data.remove(blockPosition);
//            notifyDataSetChanged();
        }

        @Override
        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
            pd.dismiss();
            Debug.e("Block", "-" + "***Failure ");
        }

        @Override
        public void onFinish() {
            super.onFinish();
            pd.dismiss();
        }
    }

    public void unBlockUser(int position) {
        pd = null;
        pd = ProgressDialog.show(mContext, "", "Loading...", true,
                false);
        try {

            JSONObject json = new JSONObject();

            json.put("fromUserId", Utils.getFromUserDefaults(mContext, Constant.PARAMS_ID));
            json.put("toUserId", data.get(position).id);

            StringEntity stringEntity = new StringEntity(json.toString());
            stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(Constant.TIMEOUT);
            client.addHeader("Token", Utils.getFromUserDefaults(mContext, Constant.PARAMS_TOKEN));

            client.post(mContext, URLs.UNBLOCKUSER, stringEntity, "application/json", new unBlockUserResponseHandler(position));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class unBlockUserResponseHandler extends AsyncHttpResponseHandler {

        int position;

        public unBlockUserResponseHandler(int position) {
            // TODO Auto-generated constructor stub
            this.position = position;
        }

        @Override
        public void onSuccess(int i, Header[] headers, byte[] responseBody) {

            pd.dismiss();

            String content = new String(responseBody);

            Debug.e("unBlock Resp", "-" + content);

            Gson gson = new Gson();
            Type type = new TypeToken<BlockInfo>() {
            }.getType();

            BlockInfo blockInfo = gson.fromJson(content, type);
            try {
                if (blockInfo.status_code == 1) {
                    data.get(position).isBlock = 0;
                    notifyDataSetChanged();
                } else {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//            data.remove(blockPosition);
//            notifyDataSetChanged();
        }

        @Override
        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
            pd.dismiss();
            Debug.e("unBlock", "-" + "***Failure ");
        }

        @Override
        public void onFinish() {
            super.onFinish();
            pd.dismiss();
        }
    }

}