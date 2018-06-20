package com.example.harsh.ml_kit.extra;

import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.example.harsh.ml_kit.R;

public class OrderGuidesActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_guides);


        RecyclerView rvOrderGuide = findViewById(R.id.rv_order_guide);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        rvOrderGuide.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        rvOrderGuide.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        OrderGuidesAdapter orderGuidesAdapter = new OrderGuidesAdapter(this);
        rvOrderGuide.setAdapter(orderGuidesAdapter);
        orderGuidesAdapter.setOnItemClickListener(new OrderGuidesAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position, Type type) {
                Toast.makeText(OrderGuidesActivity.this, "work in progress", Toast.LENGTH_SHORT).show();
                showNewOrderGuideBottomSheet();
            }
        });


    }


    /**
     * showing bottom sheet dialog
     */
    public void showNewOrderGuideBottomSheet() {

        View view = getLayoutInflater().inflate(R.layout.fragment_new_order_guide_bottom_sheet, null);

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);
        dialog.show();
    }


}

