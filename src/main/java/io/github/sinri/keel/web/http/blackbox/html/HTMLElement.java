package io.github.sinri.keel.web.http.blackbox.html;

@Deprecated(since = "3.0.10")
public class HTMLElement {
    protected String header;
    protected HTMLTagElement htmlTag;

    public HTMLElement(HTMLTagElement htmlTag) {
        this.header = "<!doctype html>";
        this.htmlTag = htmlTag;
    }

    public String toString() {
        return this.header + "\n" + this.htmlTag.toString();
    }
}
