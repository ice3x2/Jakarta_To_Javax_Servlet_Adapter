package adapter.servletElementConverter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import java.io.IOException;
import javax.servlet.ServletInputStream;

public class StreamConverter {
   public StreamConverter() {
   }

   public static ServletInputStream convert(final jakarta.servlet.ServletInputStream inputstream) {
      return new ServletInputStream() {
         @Override
         public boolean isFinished() {
            return inputstream.isFinished();
         }

         @Override
         public boolean isReady() {
            return inputstream.isReady();
         }

         @Override
         public void setReadListener(javax.servlet.ReadListener readListener) {
            inputstream.setReadListener(ReadListenerConverter.convert(readListener));

         }

         public int read() throws IOException {
            return inputstream.read();
         }

         public int read(byte[] b) throws IOException {
            return inputstream.read(b, 0, b.length);
         }

         public int read(byte[] b, int off, int len) throws IOException {
            return inputstream.readLine(b, off, len);
         }

         public long skip(long n) throws IOException {
            return inputstream.skip(n);
         }

         public int available() throws IOException {
            return inputstream.available();
         }

         public void close() throws IOException {
            inputstream.close();
         }

         public synchronized void mark(int readlimit) {
            inputstream.mark(readlimit);
         }

         public synchronized void reset() throws IOException {
            inputstream.reset();
         }

         public boolean markSupported() {
            return inputstream.markSupported();
         }
      };
   }

   public static jakarta.servlet.ServletInputStream convert(final ServletInputStream inputstream) {
      return new jakarta.servlet.ServletInputStream() {
         boolean isFinished = false;

         public int read() throws IOException {
            int value = inputstream.read();
            if (value < 0) {
               this.isFinished = true;
            }

            return value;
         }

         public int read(byte[] b) throws IOException {
            int len = inputstream.read(b, 0, b.length);
            if (len <= 0) {
               this.isFinished = true;
            }

            return len;
         }

         public int read(byte[] b, int off, int len) throws IOException {
            return inputstream.readLine(b, off, len);
         }

         public long skip(long n) throws IOException {
            return inputstream.skip(n);
         }

         public int available() throws IOException {
            return inputstream.available();
         }

         public void close() throws IOException {
            inputstream.close();
            this.isFinished = true;
         }

         public synchronized void mark(int readlimit) {
            inputstream.mark(readlimit);
         }

         public synchronized void reset() throws IOException {
            inputstream.reset();
         }

         public boolean markSupported() {
            return inputstream.markSupported();
         }

         public boolean isFinished() {
            try {
               return inputstream.isFinished();
            } catch (Exception e) {
               return this.isFinished;
            }
         }

         public boolean isReady() {
            try {
                return inputstream.isReady();
            } catch (Exception e) {
               return !this.isFinished;
            }
         }

         public void setReadListener(ReadListener arg0) {
            inputstream.setReadListener(ReadListenerConverter.convert(arg0));
         }
      };
   }

   public static ServletOutputStream convert(final javax.servlet.ServletOutputStream outputStream) {
      return new ServletOutputStream() {
         boolean isClose = false;

         public void write(int b) throws IOException {
            outputStream.write(b);
         }

         public void write(byte[] b) throws IOException {
            outputStream.write(b);
         }

         public void write(byte[] b, int off, int len) throws IOException {
            outputStream.write(b, off, len);
         }

         public void flush() throws IOException {
            outputStream.flush();
         }

         public void close() throws IOException {
            outputStream.close();
            this.isClose = true;
         }

         public boolean isReady() {
            return !this.isClose;
         }

         public void setWriteListener(WriteListener arg0) {
            outputStream.setWriteListener(WriteListenerConverter.convert(arg0));
         }
      };
   }

   public static javax.servlet.ServletOutputStream convert(final ServletOutputStream outputStream) {
      return new javax.servlet.ServletOutputStream() {
         @Override
         public boolean isReady() {
            return outputStream.isReady();
         }

         @Override
         public void setWriteListener(javax.servlet.WriteListener writeListener) {
            outputStream.setWriteListener(WriteListenerConverter.convert(writeListener));

         }

         boolean isClose = false;

         public void write(int b) throws IOException {
            outputStream.write(b);
         }

         public void write(byte[] b) throws IOException {
            outputStream.write(b);
         }

         public void write(byte[] b, int off, int len) throws IOException {
            outputStream.write(b, off, len);
         }

         public void flush() throws IOException {
            outputStream.flush();
         }

         public void close() throws IOException {
            outputStream.close();
            this.isClose = true;
         }
      };
   }
}
