package com.megadevs.adoma.sample;

import android.app.ListActivity;

public class TestActivity extends ListActivity {

    /*private List<Downloader> data = Lists.newArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setListAdapter(new MyAdapter());

        Adoma.ensureDownloads(this);

        Adoma.register(this);

        try {
            Adoma.build(this).download(new URL("http://dl.google.com/android/adt/adt-bundle-mac-x86_64-20130522.zip"));
            Adoma.build(this).download(new URL("http://dl.google.com/android/studio/android-studio-bundle-130.687321-mac.dmg"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateEvent(CreateEvent event) {
        System.out.println(String.format("Create downloading: %s", event.getKey()));
    }

    @Override
    public void onUpdateEvent(final OLDUpdateEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                data = Lists.newArrayList(event.getDownloaders().values());
                ((MyAdapter)getListAdapter()).notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onErrorEvent(OLDErrorEvent event) {
        System.out.println(String.format("Error while downloading: %s", event.getKey()));
    }

    @Override
    public void onCompleteEvent(OLDCompleteEvent event) {
        System.out.println(String.format("Download complete: %s", event.getKey()));
    }

    public class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Downloader getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new TextView(TestActivity.this);
            }
            ((TextView)convertView).setText(getItem(position).toString());
            return convertView;
        }
    }*/
}
