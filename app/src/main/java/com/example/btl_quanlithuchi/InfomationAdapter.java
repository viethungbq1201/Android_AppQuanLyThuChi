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

public class InfomationAdapter extends RecyclerView.Adapter<InfomationAdapter.ViewHolder> {
    private  List<Infomation> list;
    private final Context context;
    private final DBHelper dbHelper;

    public InfomationAdapter(Context context, List<Infomation> list) {
        this.context = context;
        this.list = list;
        this.dbHelper = new DBHelper(context);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, category, date, price;
        ImageButton btnedit, btndelete;
        ImageView anh;



        public ViewHolder(View view) {
            super(view);
            anh = view.findViewById(R.id.anh);
            title = view.findViewById(R.id.tieude);
            category = view.findViewById(R.id.danhmuc);
            date = view.findViewById(R.id.thoigian);
            price = view.findViewById(R.id.gia);
            btnedit = view.findViewById(R.id.bt_edit);
            btndelete = view.findViewById(R.id.btn_delete);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_view, parent, false);
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
        holder.title.setText(t.getTitle());
        holder.category.setText(t.getCategory());
        holder.date.setText(t.getDate());
        if (t.getType().equalsIgnoreCase("thu")) {
            holder.price.setText("+ " + formattedPrice + " Đ");
            holder.price.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.price.setText("- " + formattedPrice + " Đ");
            holder.price.setTextColor(Color.parseColor("#F44336"));
        }


        holder.btnedit.setOnClickListener(v -> showEditDialog(t, holder.getAdapterPosition()));

        holder.btndelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc muốn xóa mục này?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        Infomation item = list.get(position);

                        dbHelper.deleteInfomation(item.getId());

                        list.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, list.size());

                        Toast.makeText(context, "Đã xóa!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setData(List<Infomation> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    private void showEditDialog(Infomation info, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        Spinner spinner = view.findViewById(R.id.spinner);
        EditText edtCategory = view.findViewById(R.id.edit_category);
        EditText edtPrice = view.findViewById(R.id.edit_price);
        Button btnOk = view.findViewById(R.id.btn_ok);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        // Khởi tạo spinner
        String[] options = {"Nhập loại tiền thu chi", "Nhà cửa", "Sửa chữa", "Mua sắm", "Công việc", "Quà tặng"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Set dữ liệu cũ
        edtCategory.setText(info.getCategory());
        edtPrice.setText(String.valueOf(info.getPrice()));
        spinner.setSelection(getSpinnerPosition(info.getTitle(), options));

        btnOk.setOnClickListener(v -> {
            String newTitle = spinner.getSelectedItem().toString();
            String newCategory = edtCategory.getText().toString();
            String newPriceStr = edtPrice.getText().toString();

            if (spinner.getSelectedItemPosition() == 0) {
                Toast.makeText(context, "Vui lòng chọn loại tiền thu chi!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(newCategory) || TextUtils.isEmpty(newPriceStr)) {
                Toast.makeText(context, "Vui lòng nhập đầy đủ dữ liệu!", Toast.LENGTH_SHORT).show();
                return;
            }

            int newPrice = Integer.parseInt(newPriceStr);
            String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

            // Cập nhật object
            info.setTitle(newTitle);
            info.setCategory(newCategory);
            info.setPrice(newPrice);
            info.setDate(currentDate);

            dbHelper.updateInfomation(info);
            notifyItemChanged(position);

            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private int getSpinnerPosition(String value, String[] options) {
        for (int i = 0; i < options.length; i++) {
            if (options[i].equalsIgnoreCase(value)) return i;
        }
        return 0;
    }



}
