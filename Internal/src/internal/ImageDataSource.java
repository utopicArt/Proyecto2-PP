package internal;

import javax.media.MediaLocator;
import javax.media.Time;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;
import java.util.List;


class ImageDataSource extends PullBufferDataSource {

    ImageSourceStream streams[];

    ImageDataSource(int width, int height, int frameRate,
                    List<String> images) {
        streams = new ImageSourceStream[1];
        streams[0] = new ImageSourceStream(width, height, frameRate, images);
    }

    public void setLocator(MediaLocator source) {
    }

    public MediaLocator getLocator() { return null; }

    public String getContentType() { return ContentDescriptor.RAW; }

    public void connect() {}

    public void disconnect() {}

    public void start() {}

    public void stop() {}

    public PullBufferStream[] getStreams() {
        return streams;
    }

    public Time getDuration() {
        return DURATION_UNKNOWN;
    }

    public Object[] getControls() {
        return new Object[0];
    }

    public Object getControl(String type) {
        return null;
    }
}
