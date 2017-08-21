package forpdateam.ru.forpda.fragments.news.details;

import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.robohorse.pagerbullet.PagerBullet;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.fragments.news.details.blocks.ContentBlock;
import forpdateam.ru.forpda.fragments.news.details.blocks.GalleryBlock;
import forpdateam.ru.forpda.fragments.news.details.blocks.ImageBlock;
import forpdateam.ru.forpda.fragments.news.details.blocks.InfoBlock;
import forpdateam.ru.forpda.fragments.news.details.blocks.TitleBlock;
import forpdateam.ru.forpda.fragments.news.details.blocks.YoutubeBlock;
import forpdateam.ru.forpda.imageviewer.ImageViewerActivity;
import forpdateam.ru.forpda.imageviewer.ImagesAdapter;
import forpdateam.ru.forpda.utils.Html;
import forpdateam.ru.forpda.views.InkPageIndicator;

/**
 * Created by isanechek on 8/19/17.
 */

public class NewsDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList items = new ArrayList<>();

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return DetailsType.get(viewType).viewHolder(parent);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);
        DetailsType.get(item).bind(holder, item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return DetailsType.get(items.get(position)).type();
    }

    public void insertData(Object item) {
        items.add(item);
        notifyDataSetChanged();
    }

    public void insertData(List list) {
        for (Object o : list) {
            insertData(o);
        }
    }

    protected static class InfoHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView date;
        private TextView author;

        public InfoHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.nd_ib_title);
            author = (TextView) itemView.findViewById(R.id.nd_ib_author);
            date = (TextView) itemView.findViewById(R.id.nd_ib_date);
        }

        public void bind(InfoBlock infoBlock) {
            title.setText(infoBlock.getTitle());
            author.setText(infoBlock.getAuthor());
            date.setText(infoBlock.getDate());
        }
    }

    protected static class TitleHolder extends RecyclerView.ViewHolder {
        private TextView title;

        public TitleHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.nd_tb_tv);
        }

        public void bind(TitleBlock titleBlock) {
            title.setText(titleBlock.getTitle());
        }
    }

    protected static class ImageHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;

        public ImageHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.nd_img_block_iv);
            imageView.setClickable(true);
        }

        public void bind(ImageBlock imageBlock) {
            // title later, ept.
            ImageLoader.getInstance().displayImage(imageBlock.getImageUrl(), imageView);
            imageView.setOnClickListener(v -> ImageViewerActivity.startActivity(itemView.getContext(), imageBlock.getImageUrl()));
        }
    }

    protected static class ContentHolder extends RecyclerView.ViewHolder {

        private TextView content;

        public ContentHolder(View itemView) {
            super(itemView);
            content = (TextView) itemView.findViewById(R.id.nd_content_block_tv);
        }

        public void bind(ContentBlock contentBlock) {
            content.setText(Html.fromHtml(contentBlock.getContent()));
        }
    }

    protected static class GalleryHolder extends RecyclerView.ViewHolder {

        private TextView imagesCount;
//        private PagerBullet pager;
        private ViewPager pager;
        private InkPageIndicator mIndicator;

        public GalleryHolder(View itemView) {
            super(itemView);
            imagesCount = (TextView) itemView.findViewById(R.id.news_details_gallery_count_images);
//            pager = (PagerBullet) itemView.findViewById(R.id.news_details_gallery_count_pager);
//            pager.setIndicatorTintColorScheme(App.getColorFromAttr(itemView.getContext(), R.attr.default_text_color), App.getColorFromAttr(itemView.getContext(), R.attr.second_text_color));
            pager = (ViewPager) itemView.findViewById(R.id.news_details_gallery_count_pager);
            mIndicator = (InkPageIndicator) itemView.findViewById(R.id.news_details_gallery_count_indicator);
        }

        public void bind(GalleryBlock galleryBlock) {
            ImagesAdapter adapter = new ImagesAdapter(itemView.getContext(), galleryBlock.getUrls());
            adapter.setCropImg(true);
            pager.setAdapter(adapter);
            mIndicator.setViewPager(pager);
            adapter.setOnClickListener((view, position) -> ImageViewerActivity.startActivity(itemView.getContext(), galleryBlock.getUrls(), position));
//            if (imagesCount.getVisibility() == View.GONE) {
//                imagesCount.setVisibility(View.VISIBLE);
//                imagesCount.setText(String.valueOf(galleryBlock.getUrls().size()));
//            }



        }
    }

    protected static class YoutubeHolder extends RecyclerView.ViewHolder {
        private ImageView preview;
        public YoutubeHolder(View itemView) {
            super(itemView);
            preview = (ImageView) itemView.findViewById(R.id.nd_youtube_block_preview);
        }

        public void bind(YoutubeBlock youtubeBlock) {

        }
    }

    private enum DetailsType {
        INFO {

            @Override
            boolean is(Object item) {
                return item instanceof InfoBlock;
            }

            @Override
            int type() {
                // тут айди лайаута вместо констант
                return R.layout.news_details_info_block_layout;
            }

            @Override
            RecyclerView.ViewHolder viewHolder(ViewGroup parent) {
                return new InfoHolder(getLayout(parent, R.layout.news_details_info_block_layout));
            }

            @Override
            void bind(RecyclerView.ViewHolder holder, Object item) {
                InfoHolder infoHolder = (InfoHolder) holder;
                InfoBlock infoBlock = (InfoBlock) item;
                infoHolder.bind(infoBlock);
            }
        },
        TITLE {
            @Override
            boolean is(Object item) {
                return item instanceof TitleBlock;
            }

            @Override
            int type() {
                return R.layout.news_details_title_block_layout;
            }

            @Override
            RecyclerView.ViewHolder viewHolder(ViewGroup parent) {
                return new TitleHolder(getLayout(parent, R.layout.news_details_title_block_layout));
            }

            @Override
            void bind(RecyclerView.ViewHolder holder, Object item) {
                TitleHolder titleHolder = (TitleHolder) holder;
                TitleBlock titleBlock = (TitleBlock) item;
                titleHolder.bind(titleBlock);
            }
        },
        CONTENT {
            @Override
            boolean is(Object item) {
                return item instanceof ContentBlock;
            }

            @Override
            int type() {
                return R.layout.news_details_content_block_layout;
            }

            @Override
            RecyclerView.ViewHolder viewHolder(ViewGroup parent) {
                return new ContentHolder(getLayout(parent, R.layout.news_details_content_block_layout));
            }

            @Override
            void bind(RecyclerView.ViewHolder holder, Object item) {
                ContentHolder contentHolder = (ContentHolder) holder;
                ContentBlock contentBlock = (ContentBlock) item;
                contentHolder.bind(contentBlock);
            }
        },
        IMAGE {
            @Override
            boolean is(Object item) {
                return item instanceof ImageBlock;
            }

            @Override
            int type() {
                return R.layout.news_details_image_block_layout;
            }

            @Override
            RecyclerView.ViewHolder viewHolder(ViewGroup parent) {
                return new ImageHolder(getLayout(parent, R.layout.news_details_image_block_layout));
            }

            @Override
            void bind(RecyclerView.ViewHolder holder, Object item) {
                ImageHolder imageHolder = (ImageHolder) holder;
                ImageBlock imageBlock = (ImageBlock) item;
                imageHolder.bind(imageBlock);
            }
        },
        GALLERY {
            @Override
            boolean is(Object item) {
                return item instanceof GalleryBlock;
            }

            @Override
            int type() {
                return R.layout.news_details_gallery_block_layout;
            }

            @Override
            RecyclerView.ViewHolder viewHolder(ViewGroup parent) {
                return new GalleryHolder(getLayout(parent, R.layout.news_details_gallery_block_layout));
            }

            @Override
            void bind(RecyclerView.ViewHolder holder, Object item) {
                GalleryHolder galleryHolder = (GalleryHolder) holder;
                GalleryBlock galleryBlock = (GalleryBlock) item;
                galleryHolder.bind(galleryBlock);
            }
        },
        YOUTUBE {
            @Override
            boolean is(Object item) {
                return item instanceof YoutubeBlock;
            }

            @Override
            int type() {
                return R.layout.news_details_youtube_block_layout;
            }

            @Override
            RecyclerView.ViewHolder viewHolder(ViewGroup parent) {
                return new YoutubeHolder(getLayout(parent, R.layout.news_details_youtube_block_layout));
            }

            @Override
            void bind(RecyclerView.ViewHolder holder, Object item) {
                YoutubeHolder youtubeHolder = (YoutubeHolder) holder;
                YoutubeBlock youtubeBlock = (YoutubeBlock) item;
                youtubeHolder.bind(youtubeBlock);
            }
        };

        static DetailsType get(Object item) {
            for (DetailsType type : DetailsType.values()) {
                if (type.is(item)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Details Adapter View Type Not Found!!!");
        }

        static DetailsType get(int viewType) {
            Log.e("ADAPTER", "Get View Type " + viewType);
            for (DetailsType type : DetailsType.values()) {
                Log.e("ADAPTER", "View Type " + type.type());
                if (type.type() == viewType) {
                    return type;
                }
            }

            throw new IllegalArgumentException("Details Adapter View Type Not Found!!!");
        }

        public View getLayout(ViewGroup parent, int id) {
            return LayoutInflater.from(parent.getContext()).inflate(id, parent, false);
        }
        abstract boolean is(Object item);
        abstract int type();
        abstract RecyclerView.ViewHolder viewHolder(ViewGroup parent);
        abstract void bind(RecyclerView.ViewHolder holder, Object item);
    }
}
