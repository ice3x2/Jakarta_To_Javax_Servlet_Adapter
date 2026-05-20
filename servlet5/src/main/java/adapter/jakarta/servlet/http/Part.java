package adapter.jakarta.servlet.http;

import adapter.common.AdapterUnwrap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * 0.x 패키지 호환용 deprecated alias.
 *
 * <p>Use {@code adapter.jakarta.servlet5.http.Part} instead. 본 클래스는 v2.0
 * 에서 제거됩니다. 원본의 생성자가 package-private 이므로 composition delegate
 * 형태로 제공합니다.
 */
@Deprecated(since = "1.0.0", forRemoval = true)
public class Part implements jakarta.servlet.http.Part, AdapterUnwrap<javax.servlet.http.Part> {
    private final javax.servlet.http.Part part;

    public Part(javax.servlet.http.Part part) {
        this.part = part;
    }

    @Override public javax.servlet.http.Part unwrap() {
        return this.part;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Object other = (o instanceof AdapterUnwrap<?>) ? ((AdapterUnwrap<?>) o).unwrap() : o;
        return this.unwrap().equals(other);
    }

    @Override public int hashCode() {
        return this.unwrap().hashCode();
    }

    @Override public InputStream getInputStream() throws IOException { return this.part.getInputStream(); }
    @Override public String getContentType() { return this.part.getContentType(); }
    @Override public String getName() { return this.part.getName(); }
    @Override public long getSize() { return this.part.getSize(); }
    @Override public void write(String fileName) throws IOException { this.part.write(fileName); }
    @Override public void delete() throws IOException { this.part.delete(); }
    @Override public String getHeader(String name) { return this.part.getHeader(name); }
    @Override public Collection<String> getHeaders(String name) { return this.part.getHeaders(name); }
    @Override public Collection<String> getHeaderNames() { return this.part.getHeaderNames(); }
    @Override public String getSubmittedFileName() { return this.part.getSubmittedFileName(); }
}
