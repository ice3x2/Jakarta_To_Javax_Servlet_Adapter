package adapter.servletElementConverter;

import jakarta.servlet.descriptor.JspConfigDescriptor;
import jakarta.servlet.descriptor.JspPropertyGroupDescriptor;
import jakarta.servlet.descriptor.TaglibDescriptor;
import java.util.ArrayList;
import java.util.Collection;

public class DescriptorConverter {

   public DescriptorConverter() {
   }

   public static JspConfigDescriptor convert(final javax.servlet.descriptor.JspConfigDescriptor jspConfigDescriptor) {
      return new JspConfigDescriptor() {
         public Collection<JspPropertyGroupDescriptor> getJspPropertyGroups() {
            Collection<javax.servlet.descriptor.JspPropertyGroupDescriptor> ori = jspConfigDescriptor.getJspPropertyGroups();
            ArrayList<JspPropertyGroupDescriptor> dest = new ArrayList<>();

            for (javax.servlet.descriptor.JspPropertyGroupDescriptor item : ori) {
               dest.add(DescriptorConverter.convert(item));
            }

            return dest;
         }

         public Collection<TaglibDescriptor> getTaglibs() {
            Collection<javax.servlet.descriptor.TaglibDescriptor> ori = jspConfigDescriptor.getTaglibs();
            ArrayList<TaglibDescriptor> dest = new ArrayList<>();

            for (javax.servlet.descriptor.TaglibDescriptor item : ori) {
               dest.add(DescriptorConverter.convert(item));
            }

            return dest;
         }
      };
   }

   public static javax.servlet.descriptor.JspConfigDescriptor convert(final JspConfigDescriptor jspConfigDescriptor) {
      return new javax.servlet.descriptor.JspConfigDescriptor() {
         public Collection<javax.servlet.descriptor.JspPropertyGroupDescriptor> getJspPropertyGroups() {
            Collection<JspPropertyGroupDescriptor> ori = jspConfigDescriptor.getJspPropertyGroups();
            ArrayList<javax.servlet.descriptor.JspPropertyGroupDescriptor> dest = new ArrayList<>();

            for (JspPropertyGroupDescriptor item : ori) {
               dest.add(DescriptorConverter.convert(item));
            }

            return dest;
         }

         public Collection<javax.servlet.descriptor.TaglibDescriptor> getTaglibs() {
            Collection<TaglibDescriptor> ori = jspConfigDescriptor.getTaglibs();
            ArrayList<javax.servlet.descriptor.TaglibDescriptor> dest = new ArrayList<>();

            for (TaglibDescriptor item : ori) {
               dest.add(DescriptorConverter.convert(item));
            }

            return dest;
         }
      };
   }

   public static JspPropertyGroupDescriptor convert(final javax.servlet.descriptor.JspPropertyGroupDescriptor descriptor) {
      return new JspPropertyGroupDescriptor() {
         public String getBuffer() {
            return descriptor.getBuffer();
         }

         public String getDefaultContentType() {
            return descriptor.getDefaultContentType();
         }

         public String getDeferredSyntaxAllowedAsLiteral() {
            return descriptor.getDeferredSyntaxAllowedAsLiteral();
         }

         public String getElIgnored() {
            return descriptor.getElIgnored();
         }

         public String getErrorOnUndeclaredNamespace() {
            return descriptor.getErrorOnUndeclaredNamespace();
         }

         public Collection<String> getIncludeCodas() {
            return descriptor.getIncludeCodas();
         }

         public Collection<String> getIncludePreludes() {
            return descriptor.getIncludePreludes();
         }

         public String getIsXml() {
            return descriptor.getIsXml();
         }

         public String getPageEncoding() {
            return descriptor.getPageEncoding();
         }

         public String getScriptingInvalid() {
            return descriptor.getScriptingInvalid();
         }

         public String getTrimDirectiveWhitespaces() {
            return descriptor.getTrimDirectiveWhitespaces();
         }

         public Collection<String> getUrlPatterns() {
            return descriptor.getUrlPatterns();
         }
      };
   }

   public static javax.servlet.descriptor.JspPropertyGroupDescriptor convert(final JspPropertyGroupDescriptor descriptor) {
      return new javax.servlet.descriptor.JspPropertyGroupDescriptor() {
         public String getBuffer() {
            return descriptor.getBuffer();
         }

         public String getDefaultContentType() {
            return descriptor.getDefaultContentType();
         }

         public String getDeferredSyntaxAllowedAsLiteral() {
            return descriptor.getDeferredSyntaxAllowedAsLiteral();
         }

         public String getElIgnored() {
            return descriptor.getElIgnored();
         }

         public String getErrorOnUndeclaredNamespace() {
            return descriptor.getErrorOnUndeclaredNamespace();
         }

         public Collection<String> getIncludeCodas() {
            return descriptor.getIncludeCodas();
         }

         public Collection<String> getIncludePreludes() {
            return descriptor.getIncludePreludes();
         }

         public String getIsXml() {
            return descriptor.getIsXml();
         }

         public String getPageEncoding() {
            return descriptor.getPageEncoding();
         }

         public String getScriptingInvalid() {
            return descriptor.getScriptingInvalid();
         }

         public String getTrimDirectiveWhitespaces() {
            return descriptor.getTrimDirectiveWhitespaces();
         }

         public Collection<String> getUrlPatterns() {
            return descriptor.getUrlPatterns();
         }
      };
   }

   public static TaglibDescriptor convert(final javax.servlet.descriptor.TaglibDescriptor jspConfigDescriptor) {
      return new TaglibDescriptor() {
         public String getTaglibLocation() {
            return jspConfigDescriptor.getTaglibLocation();
         }

         public String getTaglibURI() {
            return jspConfigDescriptor.getTaglibURI();
         }
      };
   }

   public static javax.servlet.descriptor.TaglibDescriptor convert(final TaglibDescriptor jspConfigDescriptor) {
      return new javax.servlet.descriptor.TaglibDescriptor() {
         public String getTaglibLocation() {
            return jspConfigDescriptor.getTaglibLocation();
         }

         public String getTaglibURI() {
            return jspConfigDescriptor.getTaglibURI();
         }
      };
   }
}
