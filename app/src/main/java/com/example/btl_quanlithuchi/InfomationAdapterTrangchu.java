package com.example.btl_quanlithuchi;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InfomationAdapterTrangchu extends RecyclerView.Adapter<InfomationAdapterTrangchu.ViewHolder> {
    private  List<Infomation> list;
    private final Context context;
    private final DBHelper dbHelper;

    public InfomationAdapterTrangchu(Context context, List<Infomation> list) {
        this.context = context;
        this.list = list;
        this.dbHelper = new DBHelper(context);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView category, date, price;
        ImageView anh;



        public ViewHolder(View view) {
            super(view);
            anh =  view.findViewById(R.id.anh);
            category = view.findViewById(R.id.danhmuc);
            date = view.findViewById(R.id.thoigian);
            price = view.findViewById(R.id.gia);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_view_trangchu, parent, false);
        return new ViewHolder(v);
    }


    private int getImageForTitle(String title) {
        switch (title) {
            case "Nhà cửa":
                return R.drawable.ic_house;
            case "Sửa chữa":
                return R.drawable.ic_repair;
            case "Mua sắm":
                return R.drawable.ic_shopping;
            case "Công việc":
                return R.drawable.ic_working;
            case "Quà tặng":
                return R.drawable.ic_gift;
            case "Ăn uống":
                return R.drawable.ic_food;
            default:
                return R.drawable.ic_gift;

        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Infomation t = list.get(position);
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        String formattedPrice = formatter.format(t.getPrice());

        int imageRes = getImageForTitle(t.getTitle());
        holder.anh.setImageResource(imageRes);
        holder.category.setText(t.getCategory());
        holder.date.setText(t.getDate());
        if (t.getType().equalsIgnoreCase("thu")) {
            holder.price.setText("+ " + formattedPrice + " Đ");
            holder.price.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.price.setText("- " + formattedPrice + " Đ");
            holder.price.setTextColor(Color.parseColor("#F44336"));
        }


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setData(List<Infomation> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

}
