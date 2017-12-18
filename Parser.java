import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

interface CharacterFilter {
  Boolean accepts(char character);

  static final CharacterFilter NoFilter = new CharacterFilter() {
    @Override
    public Boolean accepts(char character) {
      return true;
    }
  };

  static final CharacterFilter UnicodeFilter = new CharacterFilter() {
    @Override
    public Boolean accepts(char character) {
      return character < 0x80;
    }
  };

}

interface Parser extends Closeable {
  String retrieve();

  static class InputStreamContent implements Parser {
    private final CharacterFilter filter;
    private final InputStream inputStream;

    InputStreamContent(InputStream inputStream, CharacterFilter filter) {
      this.inputStream = inputStream;
      this.filter = filter;
    }

    InputStreamContent(File file, CharacterFilter filter) {
      this(new FileInputStream(file), filter);
    }

    InputStreamContent(File file) {
      this(new FileInputStream(file), CharacterFilter.NoFilter);
    }

    @Override
    public String retrieve() {
      StringBuilder output = new StringBuilder();
      int data;
      while ((data = i.read()) != -1) {
        if (filter.accepts((char) data)) {
          output.append((char) data);
        }
      }
      return output.toString();
    }

    @Override
    public void close() throws IOException {
      this.inputStream.close();
    }
  }

  static class ParserSynchronized implements Parser {
    private final Parser parser;

    ParserSynchronized(Parser parser) {
      this.parser = parser;
    }

    @Override
    public synchronized String retrieve() {
      try (Parser p = this.parser) {
        return p.retrieve();
      }
    }

    @Override
    public synchronized void close() {
      this.parser.close();
    }
  }
}

interface Storage extends Closeable {
  void save(String content);

  static class OutputStreamStorage implements Storage {
    private OutputStream outputStream;

    OutputStreamStorage(OutputStream outputStream) {
      this.outputStream = outputStream;
    }

    OutputStreamStorage(File file) {
      this(new FileOutputStream(file));
    }

    @Override
    public void save(String content) {
      this.outputStream.write(this.content.getBytes());
      this.outputStream.flush();
    }

    @Override
    public void close() throws IOException {
      this.outputStream.close();
    }
  }

  static class StorageSynchronized implements Storage {
    private final Storage storage;

    StorageSynchronized(Storage storage) {
      this.storage = storage;
    }

    @Override
    public synchronized save(String content) {
      try (Storage s = this.storage) {
        s.save(content);
      }
    }

    @Override
    public synchronized void close() throws IOException {
      this.storage.close();
    }
  }
}
