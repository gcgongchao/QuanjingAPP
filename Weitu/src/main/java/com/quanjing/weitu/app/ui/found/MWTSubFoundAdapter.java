package com.quanjing.weitu.app.ui.found;


/**
 * Created by Administrator on 2014/12/15.
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.etsy.android.grid.util.DynamicHeightImageView;
import com.jakewharton.salvage.RecyclingPagerAdapter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.quanjing.weitu.R;
import com.quanjing.weitu.app.common.MWTCallback;
import com.quanjing.weitu.app.model.MWTNewCircle;
import com.quanjing.weitu.app.model.MWTNewCircleManager;
import com.quanjing.weitu.app.model.MWTRestManager;
import com.quanjing.weitu.app.model.MWTTalent;
import com.quanjing.weitu.app.model.MWTTalentManager;
import com.quanjing.weitu.app.protocol.MWTArticleData;
import com.quanjing.weitu.app.protocol.MWTError;
import com.quanjing.weitu.app.protocol.service.MWTArticleResult;
import com.quanjing.weitu.app.protocol.service.MWTArticleService;
import com.quanjing.weitu.app.ui.circle.NewCircleFragment;
import com.squareup.picasso.Picasso;

import org.lcsky.SVProgressHUD;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MWTSubFoundAdapter extends BaseAdapter {
    private Context context;
    private List<MWTArticleData> subArticleList;
    private MWTAutoSlidingPagerView autoSlidingPagerView;
    private static int COUNT = 10;

    public MWTSubFoundAdapter(Context context) {
        super();
        this.context = context;
    }

    //定义两个int常量标记不同的Item视图
    public static final int PIC_ITEM = 0;
    public static final int PIC_WORD_ITEM = 1;


    @Override
    public int getCount() {
        if (subArticleList != null)
            return subArticleList.size();
        else
            return 0;
    }

    @Override
    public Object getItem(int i) {
        return subArticleList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemViewType(int position) {
        MWTArticleData articleData = (MWTArticleData) getItem(position);
        if (articleData.IsRec) {
            return PIC_ITEM;
        } else {
            return PIC_WORD_ITEM;
        }
    }

    @Override
    public int getViewTypeCount() {
        //因为有两种视图，所以返回2
        return 2;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder = null;
        TopViewHolder topViewHolder = null;
        MWTArticleData articleData = subArticleList.get(position);
        if (articleData.IsRec) {
            if (convertView == null) {
                convertView = View.inflate(context, R.layout.view_found_top, null);
                topViewHolder = new TopViewHolder();
                topViewHolder.autoSlidingPagerView = (MWTAutoSlidingPagerView) convertView.findViewById(R.id.autoSlideImage);
                convertView.setTag(topViewHolder);
            } else {
                topViewHolder = (TopViewHolder) convertView.getTag();
            }
            ArrayList<String> imageIdList = new ArrayList<String>();
            imageIdList.add(articleData.CoverUrl);
            topViewHolder.autoSlidingPagerView.setAdapter(new ImagePagerAdapter(context, imageIdList));
            topViewHolder.autoSlidingPagerView.setOnPageChangeListener(new MyOnPageChangeListener());
            topViewHolder.autoSlidingPagerView.setInterval(4000);
            topViewHolder.autoSlidingPagerView.setScrollDurationFactor(2.0);
            topViewHolder.autoSlidingPagerView.startAutoScroll();

        } else {
            if (convertView == null) {
                convertView = View.inflate(context, R.layout.item_found, null);
                holder = new ViewHolder();
                holder.imageView = (ImageView) convertView.findViewById(R.id.iv_avatar);
                holder.textView = (TextView) convertView.findViewById(R.id.tv_title);
                holder.contentView = (TextView) convertView.findViewById(R.id.tv_content);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.textView.setText(articleData.Caption);
            holder.contentView.setText(articleData.Summary);
            String imageUrl = articleData.CoverUrl;
            Picasso.with(context)
                    .load(imageUrl).resize(80, 60)
                    .into(holder.imageView);
        }
        return convertView;
    }

    public MWTArticleData getTalent(int index) {
        if (subArticleList != null) {
            return subArticleList.get(index);
        } else {
            return null;
        }
    }

    public void refresh(int type, final MWTCallback callback) {
        MWTRestManager restManager = MWTRestManager.getInstance();
        MWTArticleService articleService = restManager.create(MWTArticleService.class);
        articleService.fetchSubActicles(type, 1, COUNT, new Callback<MWTArticleResult>() {
            @Override
            public void success(MWTArticleResult result, Response response) {
                subArticleList = new ArrayList<MWTArticleData>();
                subArticleList.addAll(result.article);
                notifyDataSetChanged();
                if (callback != null) {
                    callback.success();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void loadMore(int type, final MWTCallback callback) {
        MWTRestManager restManager = MWTRestManager.getInstance();
        MWTArticleService articleService = restManager.create(MWTArticleService.class);
        int page = (int) Math.ceil(subArticleList.size() * 1.0 / COUNT) + 1;
        articleService.fetchSubActicles(type, page, COUNT, new Callback<MWTArticleResult>() {
            @Override
            public void success(MWTArticleResult result, Response response) {
                if (subArticleList != null) {
                    subArticleList.addAll(result.article);
                    notifyDataSetChanged();
                }
                if (callback != null) {
                    callback.success();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void refreshIfNeeded(int type) {
        if (subArticleList == null || subArticleList.isEmpty()) {
            refresh(type, null);
        }
    }

    public void startAutoScroll() {
        if (autoSlidingPagerView != null)
            autoSlidingPagerView.startAutoScroll();
    }

    public void stopAutoScroll() {
        if (autoSlidingPagerView != null)
            autoSlidingPagerView.stopAutoScroll();
    }

    private class ViewHolder {
        ImageView imageView;
        TextView textView;
        TextView contentView;
    }

    private class TopViewHolder {
        MWTAutoSlidingPagerView autoSlidingPagerView;
    }

    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }

    public class ImagePagerAdapter extends RecyclingPagerAdapter {
        private Context context;
        private List<String> imageIdList;

        private int size;

        public ImagePagerAdapter(Context context, List<String> imageIdList) {
            this.context = context;
            this.imageIdList = imageIdList;
            this.size = imageIdList.size();
        }

        @Override
        public int getCount() {
            return imageIdList.size();
        }

        @Override
        public View getView(final int position, View view, ViewGroup container) {
            ImageViewHolder holder;
            if (view == null) {
                holder = new ImageViewHolder();
                view = holder.imageView = new ImageView(context);
                view.setTag(holder);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //int item = _autoSlidingPagerView.getCurrentItem();
                    }
                });
            } else {
                holder = (ImageViewHolder) view.getTag();
            }
            Picasso.with(context)
                    .load(imageIdList.get(position))
                    .into(holder.imageView);
            //holder.imageView.setImageResource(imageIdList.get(position));
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //toTemp(position + 1);
                }
            });
            return view;
        }

        private class ImageViewHolder {
            ImageView imageView;
        }
    }

    private void toTemp(int n) {
        Intent intent = new Intent(context, MWTSubFoundActivity.class);
        intent.putExtra("type", n);
        context.startActivity(intent);
    }
}
