package com.vpaliy.studioq.slider.adapters;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.vpaliy.studioq.R;
import com.vpaliy.studioq.common.eventBus.ChangeFilter;
import com.vpaliy.studioq.common.eventBus.EventBusProvider;
import com.zomato.photofilters.imageprocessors.Filter;

import java.util.List;

public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ThumbnailHolder> {

    private List<ThumbnailItem> filterList;
    private LayoutInflater inflater;

    public ThumbnailAdapter(Context context, @NonNull List<ThumbnailItem> filterList) {
        this.filterList=filterList;
        this.inflater=LayoutInflater.from(context);
        ThumbnailManager.processThumbnailItems(context,filterList);
    }


    public static class ThumbnailItem {
        private Bitmap bitmap;
        private Filter filter;

        public ThumbnailItem(Filter filter, Bitmap bitmap) {
            this.bitmap=bitmap;
            this.filter=filter;
        }
    }

    public int getItemCount() {
        return filterList.size();
    }

    public class ThumbnailHolder extends RecyclerView.ViewHolder{

        private ImageView image;

        public ThumbnailHolder(View itemView) {
            super(itemView);
            image = (ImageView) (itemView);
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EventBusProvider.defaultBus()
                        .post(new ChangeFilter(filterList.get(getAdapterPosition()).filter));
                }
            });
        }


        public void onBindData() {
            image.setImageBitmap(filterList.get(getAdapterPosition()).bitmap);
        }
    }

    @Override
    public ThumbnailHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.filter_image,parent,false);
        return new ThumbnailHolder(view);
    }

    @Override
    public void onBindViewHolder(ThumbnailHolder holder, int position) {
        holder.onBindData();
    }

    private static class ThumbnailManager {

        public static void processThumbnailItems(Context context, List<ThumbnailItem> itemList) {
            for(ThumbnailItem thumb:itemList) {
                float size = context.getResources().getDimension(android.R.dimen.thumbnail_width);
                thumb.bitmap = Bitmap.createScaledBitmap(thumb.bitmap, (int) size, (int) size, false);
                thumb.bitmap = thumb.filter.processFilter(thumb.bitmap);
                //cropping circle
                thumb.bitmap = generateCircularBitmap(thumb.bitmap);
            }
        }

        private  static Bitmap generateCircularBitmap(Bitmap input) {

            final int width = input.getWidth();
            final int height = input.getHeight();
            final Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            final Path path = new Path();
            path.addCircle(
                    (float) (width / 2),
                    (float) (height / 2),
                    (float) Math.min(width, (height / 2)),
                    Path.Direction.CCW
            );

            final Canvas canvas = new Canvas(outputBitmap);
            canvas.clipPath(path);
            canvas.drawBitmap(input, 0, 0, null);
            return outputBitmap;
        }

    }
}
