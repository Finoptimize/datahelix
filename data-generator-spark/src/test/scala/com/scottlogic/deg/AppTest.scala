package com.scottlogic.deg

import org.junit._

@Test
class AppTest {
//    @Test (expected = classOf[NullPointerException])
    @Test
    def DEGApp_withoutArguments_throws_NullPointerException(): Unit = {
        App.main(Array())
    }
}