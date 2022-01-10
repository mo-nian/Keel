package io.github.sinri.keel.test.mysql;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.mysql.KeelMySQLKit;
import io.github.sinri.keel.mysql.statement.WriteIntoStatement;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.Future;

import java.util.HashMap;

public class InsertConflictTest {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

        WriteIntoStatement writeIntoStatement = new WriteIntoStatement()
                .intoTable("java_test_for_sinri")
                .macroWriteOneRowWithMap(new HashMap<String, Object>() {{
                    put("name", "b");
                    put("value", 1.2);
                    put("status", "213");
                    put("record_time", KeelMySQLKit.nowAsMySQLDatetime());
                }});
//        SharedTestBootstrap.getMySQLKit().getPool()
//                .withConnection(writeIntoStatement::executeForLastInsertedID)
//                .compose(lastInsertedID->{
//                    System.out.println("lastInsertedID "+lastInsertedID);
//                    return Future.succeededFuture();
//                })
//                .onFailure(throwable -> {
//                    System.out.println("FAILED: "+throwable.getMessage()+" / "+ throwable.getClass());
//                })
//                .eventually(v->Keel.getVertx().close());
        SharedTestBootstrap.getMySQLKit().getPool()
                .withConnection(writeIntoStatement::executeForAffectedRows)
                .compose(afx -> {
                    System.out.println("AFX " + afx);
                    return Future.succeededFuture();
                })
                .onFailure(throwable -> {
                    System.out.println("FAILED: " + throwable.getMessage() + " / " + throwable.getClass());
                })
                .eventually(v -> Keel.getVertx().close());
    }
}
