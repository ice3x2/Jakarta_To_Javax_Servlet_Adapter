package adapter.servletElementConverter5;

import jakarta.servlet.descriptor.JspConfigDescriptor;
import jakarta.servlet.descriptor.JspPropertyGroupDescriptor;
import jakarta.servlet.descriptor.TaglibDescriptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * G8 — {@link adapter.servletElementConverter5.DescriptorConverter} 6 factory +
 * collection 변환 단위 테스트 (T-PH005-07).
 *
 * <p>외부 servlet API ({@code javax.servlet.descriptor.*}, {@code jakarta.servlet.descriptor.*})
 * 만 mock; SUT 의 변환 결과 인스턴스는 mock 하지 않는다. RETURNS_SMART_NULLS 적용
 * (FR-TEST-001 AC-3).
 *
 * <p>위임/변환 cover: {@code TaglibDescriptor↔} 양방향, {@code JspPropertyGroupDescriptor↔}
 * 양방향 (대표 getter), {@code JspConfigDescriptor↔} empty / non-empty collection 변환.
 *
 * @req FR-TEST-001
 */
class DescriptorConverterUnitTest {

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: convert(javax.TaglibDescriptor) → jakarta TaglibDescriptor 위임")
    void shouldConvertTaglibDescriptorJavaxToJakarta_AC2_78_01() {
        javax.servlet.descriptor.TaglibDescriptor origin =
                mock(javax.servlet.descriptor.TaglibDescriptor.class,
                        withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        when(origin.getTaglibLocation()).thenReturn("/WEB-INF/tld/a.tld");
        when(origin.getTaglibURI()).thenReturn("http://example.com/tld/a");

        TaglibDescriptor sut = DescriptorConverter.convert(origin);
        String location = sut.getTaglibLocation();
        String uri = sut.getTaglibURI();

        verify(origin).getTaglibLocation();
        verify(origin).getTaglibURI();
        assertEquals("/WEB-INF/tld/a.tld", location);
        assertEquals("http://example.com/tld/a", uri);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: convert(jakarta.TaglibDescriptor) → javax TaglibDescriptor 위임 (mirror)")
    void shouldConvertTaglibDescriptorJakartaToJavax_AC2_78_02() {
        TaglibDescriptor origin = mock(TaglibDescriptor.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        when(origin.getTaglibLocation()).thenReturn("/WEB-INF/tld/b.tld");
        when(origin.getTaglibURI()).thenReturn("http://example.com/tld/b");

        javax.servlet.descriptor.TaglibDescriptor sut = DescriptorConverter.convert(origin);
        String location = sut.getTaglibLocation();
        String uri = sut.getTaglibURI();

        verify(origin).getTaglibLocation();
        verify(origin).getTaglibURI();
        assertEquals("/WEB-INF/tld/b.tld", location);
        assertEquals("http://example.com/tld/b", uri);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: convert(javax.JspPropertyGroupDescriptor) — 대표 getter (getBuffer/getPageEncoding) 위임")
    void shouldConvertJspPropertyGroupDescriptor_AC2_78_03() {
        javax.servlet.descriptor.JspPropertyGroupDescriptor origin =
                mock(javax.servlet.descriptor.JspPropertyGroupDescriptor.class,
                        withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        when(origin.getBuffer()).thenReturn("8kb");
        when(origin.getPageEncoding()).thenReturn("UTF-8");

        JspPropertyGroupDescriptor sut = DescriptorConverter.convert(origin);
        String buffer = sut.getBuffer();
        String enc = sut.getPageEncoding();

        verify(origin).getBuffer();
        verify(origin).getPageEncoding();
        assertEquals("8kb", buffer);
        assertEquals("UTF-8", enc);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: convert(jakarta.JspPropertyGroupDescriptor) → javax (mirror) — getElIgnored/getScriptingInvalid 위임")
    void shouldConvertJspPropertyGroupDescriptorMirror_AC2_78_04() {
        JspPropertyGroupDescriptor origin = mock(JspPropertyGroupDescriptor.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        when(origin.getElIgnored()).thenReturn("true");
        when(origin.getScriptingInvalid()).thenReturn("false");

        javax.servlet.descriptor.JspPropertyGroupDescriptor sut = DescriptorConverter.convert(origin);
        String el = sut.getElIgnored();
        String sc = sut.getScriptingInvalid();

        verify(origin).getElIgnored();
        verify(origin).getScriptingInvalid();
        assertEquals("true", el);
        assertEquals("false", sc);
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: convert(javax.JspConfigDescriptor) — empty collection 변환 통과")
    void shouldConvertEmptyJspConfigDescriptor_AC2_78_05() {
        javax.servlet.descriptor.JspConfigDescriptor origin =
                mock(javax.servlet.descriptor.JspConfigDescriptor.class,
                        withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        when(origin.getJspPropertyGroups()).thenReturn(Collections.emptyList());
        when(origin.getTaglibs()).thenReturn(Collections.emptyList());

        JspConfigDescriptor sut = DescriptorConverter.convert(origin);
        Collection<JspPropertyGroupDescriptor> groups = sut.getJspPropertyGroups();
        Collection<TaglibDescriptor> taglibs = sut.getTaglibs();

        verify(origin).getJspPropertyGroups();
        verify(origin).getTaglibs();
        assertNotNull(groups);
        assertNotNull(taglibs);
        assertTrue(groups.isEmpty());
        assertTrue(taglibs.isEmpty());
    }

    /**
     * @req FR-TEST-001
     */
    @Test
    @DisplayName("FR-TEST-001 AC-2: convert(javax.JspConfigDescriptor) — non-empty collection 순서 보존")
    void shouldConvertNonEmptyJspConfigDescriptorAndPreserveOrder_AC2_78_06() {
        javax.servlet.descriptor.TaglibDescriptor t1 =
                mock(javax.servlet.descriptor.TaglibDescriptor.class,
                        withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        when(t1.getTaglibURI()).thenReturn("uri-1");
        javax.servlet.descriptor.TaglibDescriptor t2 =
                mock(javax.servlet.descriptor.TaglibDescriptor.class,
                        withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        when(t2.getTaglibURI()).thenReturn("uri-2");
        javax.servlet.descriptor.JspConfigDescriptor origin =
                mock(javax.servlet.descriptor.JspConfigDescriptor.class,
                        withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        when(origin.getJspPropertyGroups()).thenReturn(Collections.emptyList());
        java.util.List<javax.servlet.descriptor.TaglibDescriptor> originList = new java.util.ArrayList<>();
        originList.add(t1);
        originList.add(t2);
        when(origin.getTaglibs()).thenReturn(originList);

        JspConfigDescriptor sut = DescriptorConverter.convert(origin);
        Collection<TaglibDescriptor> taglibs = sut.getTaglibs();

        verify(origin).getTaglibs();
        assertEquals(2, taglibs.size());
        java.util.Iterator<TaglibDescriptor> it = taglibs.iterator();
        TaglibDescriptor first = it.next();
        TaglibDescriptor second = it.next();
        // 변환된 어댑터 인스턴스의 getter 위임이 원본 순서 t1, t2 로 보존되어야 한다
        assertEquals("uri-1", first.getTaglibURI());
        assertEquals("uri-2", second.getTaglibURI());
    }
}
