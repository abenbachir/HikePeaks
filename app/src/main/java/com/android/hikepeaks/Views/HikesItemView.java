package com.android.hikepeaks.Views;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.hikepeaks.Models.Trail;
import com.android.hikepeaks.Services.TerminalManager;
import com.android.hikepeaks.R;
import com.bumptech.glide.Glide;

public class HikesItemView extends LinearLayout {

    private Trail model;
    private int resource;
    private TextView ownerName;
    private TextView title;
    private ImageView ownerPicture;
    private ImageView bgImage;
    private TerminalManager terminalManager = TerminalManager.getInstance();

    /**
     * Construct a CellView
     *
     * @param context, model
     */
    public HikesItemView(Context context, Trail model, int resource) {
        super(context);
        this.model = model;
        this.resource = resource;
        init();
    }


    private void init() {
        View view = inflate(getContext(), resource, this);

        this.ownerName = (TextView) findViewById(R.id.hike_owner_name);
        this.ownerPicture = (ImageView) findViewById(R.id.hike_owner_picture);
        this.title = (TextView) findViewById(R.id.hike_title);
        this.bgImage = (ImageView) findViewById(R.id.item_image_bg);

        if(title != null)
            title.setTextColor(Color.WHITE);
        if(ownerName != null)
            ownerName.setTextColor(Color.WHITE);

        updateControls();
    }

    public void updateControls() {
        if(title!=null)
            title.setText(model.getTitle());
        if(ownerName!=null)
            ownerName.setText("By " + model.getAccount().getDisplayName());

        if(ownerPicture != null) {
            try {
                Glide.with(getContext())
                        .load(model.getAccount().getPictureUrl())
                        .into(ownerPicture);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(bgImage != null) {
            try {
                bgImage.setImageResource(android.R.color.transparent);
                if (model.getCaptures().size() > 0) {
                    String firstCaptureUrl = model.getCaptures().get(0).getPicturePath();
                    Glide.with(getContext())
                            .load(firstCaptureUrl)
                            .centerCrop()
                            .into(bgImage);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Trail getModel() {
        return model;
    }

    public void setModel(Trail model) {
        this.model = model;
    }


}
