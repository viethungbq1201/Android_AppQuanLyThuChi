package com.example.btl_quanlithuchi;

import static android.text.TextUtils.isEmpty;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Trangchi_Fragment extends Fragment {

    private RecyclerView rc_view_3;
    private InfomationAdapter adapter;
    private DBHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.trang_chi, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

        dbHelper = new DBHelper(getContext());
        rc_view_3 = view.findViewById(R.id.rc_view_3);
        rc_view_3.setLayoutManager(new LinearLayoutManager(getContext()));
        List<Infomation> list = dbHelper.getInfomationsByType("chi");
        adapter = new InfomationAdapter(getContext(), list);
        rc_view_3.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.add_wallet_entry_fab);


        fab.setOnClickListener(v -> {
            showAddEntryDialog();
        });

        return view;
    }

    private void showAddEntryDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add, null);
        builder.setView(view);

        TextView textType = view.findViewById(R.id.text_type);
        textType.setText("CHI");

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        Button btnOk = view.findViewById(R.id.btn_ok);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        Spinner spinner = view.findViewById(R.id.spinner);
        String[] options = {"Nhập loại tiền thu chi", "Công việc", "Nhà cửa", "Ăn uống", "Sửa chữa", "Mua sắm", "Quà tặng"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        TextView tvSelectedDate = view.findViewById(R.id.tv_selected_date);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdfFull = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        SimpleDateFormat sdfDateOnly = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        String currentDate = sdfDateOnly.format(calendar.getTime());
        String currentDateTime = sdfFull.format(calendar.getTime());
        tvSelectedDate.setText("Ngày: " + currentDateTime);

        final String[] selectedDateTime = { currentDateTime };

        tvSelectedDate.setOnClickListener(v1 -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (view1, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth, hour, minute, second); // giữ nguyên giờ
                        selectedDateTime[0] = sdfFull.format(calendar.getTime());
                        tvSelectedDate.setText("Ngày: " + sdfFull.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });


        btnOk.setOnClickListener(v -> {

            DBHelper dbHelper = new DBHelper(getContext());
            Infomation inf = new Infomation();

            String category = ((EditText)view.findViewById(R.id.edit_category)).getText().toString();
            String price = ((EditText)view.findViewById(R.id.edit_price)).getText().toString();


            int position = spinner.getSelectedItemPosition();
            if (position == 0) {
                Toast.makeText(getContext(), "Vui lòng chọn loại tiền thu chi!", Toast.LENGTH_SHORT).show();
            } else if (isEmpty(category)) {
                Toast.makeText(getContext(), "Vui lòng nhập danh mục!", Toast.LENGTH_SHORT).show();
            } else if (isEmpty(price)) {
                Toast.makeText(getContext(), "Vui lòng nhập giá tiền!", Toast.LENGTH_SHORT).show();
            } else {
                String selected = spinner.getSelectedItem().toString();

                inf.setTitle(selected);
                inf.setCategory(category);
                inf.setPrice(Integer.parseInt(price));
                inf.setType("chi");  // "chi" nếu ở Trangchi
                inf.setDate(selectedDateTime[0]);  // chứa cả ngày và giờ
                dbHelper.insertInfomation(inf);

                List<Infomation> list = dbHelper.getInfomationsByType("chi");
                InfomationAdapter adapters = new InfomationAdapter(getContext(),list);
                rc_view_3.setLayoutManager(new LinearLayoutManager(getContext()));
                rc_view_3.setAdapter(adapters);

                dialog.dismiss();
            }

            int income = dbHelper.getTotalIncome();
            int expense = dbHelper.getTotalExpense();
            int balance = income - expense;
            if (balance < 0) {
                sendNotification("⚠️ Số dư đã âm! Bạn nghèo rồi.");
            }

        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }


    private void sendNotification(String message) {
        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "sodu_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Số dư cảnh báo", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), channelId)
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle("Cảnh báo số dư")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager.notify(1, builder.build());
    }





    @Override
    public void onResume() {
        super.onResume();

        if (dbHelper != null && adapter != null) {
            List<Infomation> updatedList = dbHelper.getInfomationsByType("chi");
            adapter.setData(updatedList);

        }
    }

}
