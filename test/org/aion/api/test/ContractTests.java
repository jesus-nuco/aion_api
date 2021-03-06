/*
 * Copyright (c) 2017-2018 Aion foundation.
 *
 *     This file is part of the aion network project.
 *
 *     The aion network project is free software: you can redistribute it
 *     and/or modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation, either version 3 of
 *     the License, or any later version.
 *
 *     The aion network project is distributed in the hope that it will
 *     be useful, but WITHOUT ANY WARRANTY; without even the implied
 *     warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *     See the GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with the aion network project source files.
 *     If not, see <https://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Aion foundation.
 */

package org.aion.api.test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.aion.api.ITx.NRG_LIMIT_CONTRACT_CREATE_MAX;
import static org.aion.api.ITx.NRG_LIMIT_TX_MAX;
import static org.aion.api.ITx.NRG_PRICE_MIN;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.aion.api.IAionAPI;
import org.aion.api.IContract;
import org.aion.api.ITx;
import org.aion.api.IUtils;
import org.aion.api.impl.AionAPIImpl;
import org.aion.api.sol.IAddress;
import org.aion.api.sol.IBool;
import org.aion.api.sol.IBytes;
import org.aion.api.sol.IDynamicBytes;
import org.aion.api.sol.ISString;
import org.aion.api.sol.ISolidityArg;
import org.aion.api.sol.IUint;
import org.aion.api.type.ApiMsg;
import org.aion.api.type.ContractResponse;
import org.aion.base.type.Address;
import org.aion.base.util.ByteArrayWrapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ContractTests {

    // Make sure the password of the testing account been set properly
    private String pw = "PLAT4life";

    private IAionAPI api;

    private static String readFile(String fileName) {
        StringBuilder contract = new StringBuilder();
        Scanner s = new Scanner(ContractTests.class.getResourceAsStream("./contract/" + fileName));
        while (s.hasNextLine()) {
            contract.append(s.nextLine());
            contract.append("\n");
        }
        s.close();

        return contract.toString();
    }

    private void connectAPI() {
        ApiMsg apiMsg = api.connect(AionAPIImpl.LOCALHOST_URL);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());
    }

    private boolean isEnoughBalance(Address address) {
        ApiMsg apiMsg = api.getChain().getBalance(address);
        assertFalse(apiMsg.isError());
        BigInteger balance = apiMsg.getObject();
        assertNotNull(balance);

        return balance.compareTo(BigInteger.valueOf(ITx.NRG_LIMIT_CONTRACT_CREATE_MAX)
            .multiply(BigInteger.valueOf(ITx.NRG_PRICE_MIN))) > 0;
    }

    @Before
    public void Setup() {
        api = IAionAPI.init();
    }

    @Test(timeout = 180000)
    public void TestCreateContractFromSource() {

        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());
        List accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        Address acc = (Address) accs.get(0);
        if (!isEnoughBalance(acc)) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        apiMsg = api.getWallet().unlockAccount(acc, pw, 300);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        String tc = readFile("testContract.sol");

        apiMsg = api.getContractController()
            .createFromSource(tc, acc, NRG_LIMIT_CONTRACT_CREATE_MAX, NRG_PRICE_MIN);
        assertFalse(apiMsg.isError());

        IContract contract = api.getContractController().getContract();
        assertNotNull(contract);

        assertEquals(contract.getFrom(), acc);
        assertNotNull(contract.getContractAddress());
        assertEquals(8, contract.getAbiDefinition().size());
        api.destroyApi();
    }


    @Test(timeout = 180000)
    public void TestCreateContract2() {
        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());
        List accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        Address acc = (Address) accs.get(0);
        if (!isEnoughBalance(acc)) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        apiMsg = api.getWallet().unlockAccount(acc, pw, 300);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        String tc = readFile("byteArrayMap.sol");

        apiMsg = api.getContractController()
            .createFromSource(tc, acc, NRG_LIMIT_CONTRACT_CREATE_MAX, NRG_PRICE_MIN);
        assertFalse(apiMsg.isError());

        IContract contract = api.getContractController().getContract();
        assertNotNull(contract);
        assertEquals(contract.getFrom(), acc);
        assertNotNull(contract.getContractAddress());
        assertEquals(3, contract.getAbiDefinition().size());

        api.destroyApi();
    }

    @Test
    public void TestCreateContractFromAddress() {

        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());
        List accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        Address acc = (Address) accs.get(0);
        if (!isEnoughBalance(acc)) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        apiMsg = api.getWallet().unlockAccount(acc, pw, 300);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        String tc = readFile("testContract.sol");

        apiMsg = api.getContractController()
            .createFromSource(tc, acc, NRG_LIMIT_CONTRACT_CREATE_MAX, NRG_PRICE_MIN);
        assertFalse(apiMsg.isError());

        IContract ct = api.getContractController().getContract();
        assertNotNull(ct);

        Address ctAddr = ct.getContractAddress();

        String abiDef = readFile("testContract.abi");

        IContract ctAt = api.getContractController()
            .getContractAt(acc, ctAddr, abiDef);

        assertFalse(ctAt.error());

        IContract contract = api.getContractController().getContract();
        assertEquals(contract.getContractAddress(), ctAddr);
        assertEquals(0, contract.getInputParams().size());
        assertEquals(0, contract.getOutputParams().size());

        api.destroyApi();
    }

    @Test
    public void TestCallWithParameterDecode() {

        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());
        List accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        Address acc = (Address) accs.get(0);
        if (!isEnoughBalance(acc)) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        apiMsg = api.getWallet().unlockAccount(acc, pw, 300);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        String sc = readFile("solTypes.sol");
        apiMsg = api.getContractController()
            .createFromSource(sc, acc, NRG_LIMIT_CONTRACT_CREATE_MAX, NRG_PRICE_MIN);
        assertFalse(apiMsg.isError());

        IContract ct = api.getContractController().getContract();
        assertEquals(ct.getFrom(), acc);
        assertNotNull(ct.getContractAddress());

        apiMsg = ct.newFunction("getValues")
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute();

        assertFalse(apiMsg.isError());
        ContractResponse cr = apiMsg.getObject();
        assertNotNull(ct);

        assertEquals(1234L, (long) (Long) cr.getData().get(0));
        assertTrue((boolean) cr.getData().get(1));
        assertEquals(ByteArrayWrapper.wrap((byte[]) cr.getData().get(2))
            , ByteArrayWrapper.wrap(
                Address.wrap("1234567890123456789012345678901234567890123456789012345678901234")
                    .toBytes()));
        assertEquals(ByteArrayWrapper.wrap((byte[]) cr.getData().get(3))
            , ByteArrayWrapper.wrap(Objects.requireNonNull(IUtils
                .hex2Bytes("1234567890123456789012345678901234567890123456789012345678901234"))));
        assertTrue(((String) cr.getData().get(4)).contentEquals("Aion!"));
        assertEquals((long) ((Long) cr.getData().get(5)), -1234L);

        api.destroyApi();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void TestCallGetStaticArray() {

        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());
        List accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        Address acc = (Address) accs.get(0);
        if (!isEnoughBalance(acc)) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        apiMsg = api.getWallet().unlockAccount(acc, pw, 300);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        String sc = readFile("solArrayTypes.sol");
        apiMsg = api.getContractController()
            .createFromSource(sc, acc, NRG_LIMIT_CONTRACT_CREATE_MAX, NRG_PRICE_MIN);
        assertFalse(apiMsg.isError());

        IContract ct = api.getContractController().getContract();
        assertEquals(ct.getFrom(), acc);
        assertNotNull(ct.getContractAddress());

        apiMsg = ct.newFunction("getStaticArray")
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute();
        assertFalse(apiMsg.isError());

        ContractResponse cr = apiMsg.getObject();

        List<Boolean> bool = Arrays.asList(true, false, true, false);
        List<Address> addr = Arrays.asList(
            Address.wrap("a011111111111111111111111111111111111111111111111111111111111111"),
            Address.wrap("a022222222222222222222222222222222222222222222222222222222222222"),
            Address.wrap("a033333333333333333333333333333333333333333333333333333333333333"),
            Address.wrap("a044444444444444444444444444444444444444444444444444444444444444"));

        List<Long> uint = Arrays.asList(1111L, 2222L, 3333L, 4444L);
        assertEquals(cr.getData().get(0), bool);

        List<byte[]> addrAry = (List<byte[]>) cr.getData().get(1);
        List<Address> addrTran = addrAry.stream().map(Address::wrap).collect(Collectors.toList());

        IntStream.range(0, 4).forEach(i -> {
            assertEquals(addrTran.get(i), addr.get(i));
        });
        assertEquals(cr.getData().get(2), uint);

        api.destroyApi();
    }

    @Test
    @Ignore
    public void TestCallGetTuple() {
        api.connect(AionAPIImpl.LOCALHOST_URL);
        api.getContractController().clear();

        String abiDefinition = "[{\"constant\":true,\"inputs\":[],\"name\":\"getValues\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128\"},{\"name\":\"\",\"type\":\"bool\"},{\"name\":\"\",\"type\":\"address\"},{\"name\":\"\",\"type\":\"bytes32\"},{\"name\":\"\",\"type\":\"string\"},{\"name\":\"\",\"type\":\"int128\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"boolVal\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"name\":\"intValArr\",\"outputs\":[{\"name\":\"\",\"type\":\"int128\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"addressVal\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"name\":\"uintValArr\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"name\":\"bytes32ValArr\",\"outputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"stringVal\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"setValues\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"intVal\",\"outputs\":[{\"name\":\"\",\"type\":\"int128\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"name\":\"uintBoolValArr\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"uintVal\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"bytes32Val\",\"outputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"getTuple\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128\"},{\"name\":\"\",\"type\":\"uint128[5]\"},{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"name\":\"uintAddressValArr\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"getArrays\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128[5]\"},{\"name\":\"\",\"type\":\"bool[5]\"}],\"payable\":false,\"type\":\"function\"}]\n";

        Address cb = ((List<Address>) api.getWallet().getAccounts().getObject()).get(0);
        assertNotNull(cb);
        assertTrue(api.getWallet().unlockAccount(cb, pw, 3600).getObject());

        Address contractAddress = Address
            .wrap("cf7eda19f9ef89a12a7abb708f8cc84cccf3c21d123412341234123412341234");

        IContract ct = api.getContractController()
            .getContractAt(cb, contractAddress, abiDefinition);
        assertNotNull(ct);

        if (ct.error()) {
            System.out.println("deploy contract failed! " + ct.getErrString());
        }

        ContractResponse cr = ct.newFunction("getTuple")
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute()
            .getObject();

        assertNotNull(cr);

        //TODO:
        //adding data check

        api.destroyApi();
    }

    // Todo: rewrite test later
    @Test
    @Ignore
    public void TestCallGetTupleWithString() {
        api.connect(AionAPIImpl.LOCALHOST_URL);
        api.getContractController().clear();

        String abiDefinition = "[{\"constant\":true,\"inputs\":[],\"name\":\"getValues\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128\"},{\"name\":\"\",\"type\":\"bool\"},{\"name\":\"\",\"type\":\"address\"},{\"name\":\"\",\"type\":\"bytes32\"},{\"name\":\"\",\"type\":\"string\"},{\"name\":\"\",\"type\":\"int128\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"boolVal\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"name\":\"intValArr\",\"outputs\":[{\"name\":\"\",\"type\":\"int128\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"addressVal\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"name\":\"uintValArr\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"name\":\"bytes32ValArr\",\"outputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"stringVal\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"setValues\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"intVal\",\"outputs\":[{\"name\":\"\",\"type\":\"int128\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"getTupleWithString\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128\"},{\"name\":\"\",\"type\":\"uint128[5]\"},{\"name\":\"\",\"type\":\"string\"},{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"name\":\"uintBoolValArr\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"uintVal\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"bytes32Val\",\"outputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"getTuple\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128\"},{\"name\":\"\",\"type\":\"uint128[5]\"},{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint128\"}],\"name\":\"uintAddressValArr\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"getArrays\",\"outputs\":[{\"name\":\"\",\"type\":\"uint128[5]\"},{\"name\":\"\",\"type\":\"bool[5]\"}],\"payable\":false,\"type\":\"function\"}]\n";

        Address cb = ((List<Address>) api.getWallet().getAccounts().getObject()).get(0);
        assertNotNull(cb);
        assertTrue(api.getWallet().unlockAccount(cb, pw, 3600).getObject());

        Address contractAddress = Address.wrap("825bdcc890ea6ed616f00424bb893cef93703b00");

        IContract ct = api.getContractController()
            .getContractAt(cb, contractAddress, abiDefinition);
        assertNotNull(ct);

        if (ct.error()) {
            System.out.println("deploy contract failed! " + ct.getErrString());
        }

        ContractResponse cr = ct.newFunction("getTupleWithString")
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute()
            .getObject();

        assertNotNull(cr);

        //TODO:
        //adding data check

        api.destroyApi();
    }

    @Test
    @Ignore
    public void TestCallGetPrescription() {

        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());
        List accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        Address acc = (Address) accs.get(0);
        if (!isEnoughBalance(acc)) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        apiMsg = api.getWallet().unlockAccount(acc, pw, 300);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        String sc = readFile("prescription.sol");
        apiMsg = api.getContractController()
            .createFromSource(sc, acc, NRG_LIMIT_CONTRACT_CREATE_MAX, NRG_PRICE_MIN);
        assertFalse(apiMsg.isError());

        IContract ct = api.getContractController().getContract();
        assertEquals(ct.getFrom(), acc);
        assertNotNull(ct.getContractAddress());

        assertEquals(ct.getFrom(), acc);
        assertNotNull(ct.getContractAddress());
        assertEquals(15, ct.getAbiDefinition().size());
        assertEquals(2, ct.getContractEventList().size());

        String abi = readFile("prescription.abi");
        IContract ct2 = api.getContractController()
            .getContractAt(acc, ct.getContractAddress(), abi);
        assertNotNull(ct2);

        apiMsg = ct2.newFunction("prescribe")
            .setParam(IAddress.copyFrom(
                Address.wrap("0000000000000000000000000000000000000000000000000000000000000001")
                    .toBytes()))
            .setParam(ISString.copyFrom("drug"))
            .setParam(IUint.copyFrom(25L))
            .setParam(IAddress.copyFrom(
                Address.wrap("0000000000000000000000000000000000000000000000000000000000001234")
                    .toBytes()))
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute();

        assertFalse(apiMsg.isError());

        ContractResponse cr = apiMsg.getObject();
        assertNotNull(cr);

        cr = ct2.newFunction("prescription")
            .setParam(IAddress.copyFrom(
                Address.wrap("0000000000000000000000000000000000000000000000000000000000001234")
                    .toBytes()))
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute()
            .getObject();

        List<Object> oparams = cr.getData();

        assertThat(oparams.get(1), is(equalTo(
            IUtils.hex2Bytes("0000000000000000000000000000000000000000000000000000000000000000"))));
        assertThat(oparams.get(2), is(equalTo("drug")));
        assertThat(oparams.get(3), is(equalTo(25L)));
        assertThat(oparams.get(4), is(equalTo(false)));
        assertThat(oparams.get(5), is(equalTo(0L)));

        api.destroyApi();
    }

    @Test
    public void TestTransactionParameterBytes() {
        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());
        List accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        Address acc = (Address) accs.get(0);
        if (!isEnoughBalance(acc)) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        apiMsg = api.getWallet().unlockAccount(acc, pw, 300);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        String tc = readFile("testContract2.sol");

        apiMsg = api.getContractController()
            .createFromSource(tc, acc, NRG_LIMIT_CONTRACT_CREATE_MAX, NRG_PRICE_MIN);
        assertFalse(apiMsg.isError());

        IContract ct = api.getContractController().getContract();
        assertNotNull(ct);
        assertEquals(ct.getFrom(), acc);
        assertNotNull(ct.getContractAddress());

        apiMsg = ct.newFunction("input32")
            .setParam(IBytes.copyFrom(
                new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 1}))
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute();

        assertFalse(apiMsg.isError());

        ContractResponse cr = apiMsg.getObject();
        System.out.println("Response Hash: " + cr.getTxHash().toString());

        apiMsg = ct.newFunction("a")
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute();
        assertFalse(apiMsg.isError());

        cr = apiMsg.getObject();

        assertThat(cr.getData().get(0), is(equalTo(
            new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 1})));

        apiMsg = ct.newFunction("inputS")
            .setParam(ISString.copyFrom("25"))
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute();
        assertFalse(apiMsg.isError());

        cr = apiMsg.getObject();

        System.out.println("Response Hash: " + cr.getTxHash().toString());

        apiMsg = ct.newFunction("e")
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute();
        assertFalse(apiMsg.isError());

        cr = apiMsg.getObject();

        apiMsg = ct.newFunction("input")
            .setParam(IDynamicBytes.copyFrom(new byte[]{2, 5}))
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute();
        assertFalse(apiMsg.isError());

        cr = apiMsg.getObject();

        System.out.println("Response Hash: " + cr.getTxHash().toString());

        apiMsg = ct.newFunction("b")
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute();
        assertFalse(apiMsg.isError());

        cr = apiMsg.getObject();

        assertTrue(Arrays.equals((byte[]) cr.getData().get(0), new byte[]{2, 5}));

        apiMsg = ct.newFunction("input8")
            .setParam(IBytes.copyFrom(new byte[]{0, 0, 0, 0, 0, 0, 0, 1}))
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute();
        assertFalse(apiMsg.isError());

        cr = apiMsg.getObject();

        System.out.println("Response Hash: " + cr.getTxHash().toString());

        apiMsg = ct.newFunction("c")
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute();
        assertFalse(apiMsg.isError());

        cr = apiMsg.getObject();

        assertThat(cr.getData().get(0),
            is(equalTo(new byte[]{0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0})));

        cr = ct.newFunction("input16")
            .setParam(IBytes.copyFrom(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}))
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute()
            .getObject();

        System.out.println("Response Hash: " + cr.getTxHash().toString());

        apiMsg = ct.newFunction("d")
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute();
        assertFalse(apiMsg.isError());

        cr = apiMsg.getObject();

        assertThat(cr.getData().get(0),
            is(equalTo(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1})));

        api.destroyApi();
    }

    @Test
    public void TestTransactionWithDynamicParam() {
        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());
        List accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        Address acc = (Address) accs.get(0);
        if (!isEnoughBalance(acc)) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        apiMsg = api.getWallet().unlockAccount(acc, pw, 300);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        String ticker = readFile("ticker.sol");

        apiMsg = api.getContractController()
            .createFromSource(ticker, acc, NRG_LIMIT_CONTRACT_CREATE_MAX, NRG_PRICE_MIN);
        assertFalse(apiMsg.isError());

        IContract contract = api.getContractController().getContract();
        assertNotNull(contract);

        List<Integer> inputUint = new ArrayList<>();
        inputUint.add(1);
        inputUint.add(2);
        inputUint.add(3);
        inputUint.add(4);

        apiMsg = contract.newFunction("tick")
            .setParam(IUint.copyFrom(inputUint))
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute();

        assertFalse(apiMsg.isError());

        ContractResponse contractResponse = apiMsg.getObject();

        System.out.println("Hash Response: " + contractResponse.getTxHash().toString());
        api.destroyApi();
    }

    @Test
    public void TestTransactionWithMoreDynamicParam() {
        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());
        List accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        Address acc = (Address) accs.get(0);
        if (!isEnoughBalance(acc)) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        apiMsg = api.getWallet().unlockAccount(acc, pw, 300);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        String ticker = readFile("ticker2.sol");

        apiMsg = api.getContractController()
            .createFromSource(ticker, acc, NRG_LIMIT_CONTRACT_CREATE_MAX, NRG_PRICE_MIN);
        assertFalse(apiMsg.isError());

        IContract contract = api.getContractController().getContract();
        assertNotNull(contract);

        ArrayList<Integer> inputUint = new ArrayList<>();
        inputUint.add(1);
        inputUint.add(2);
        inputUint.add(3);
        inputUint.add(4);

        ArrayList<String> inputAddress = new ArrayList<>();
        inputAddress.add("0000000000000000000000000000000000000000000000000000000000001001");
        inputAddress.add("0000000000000000000000000000000000000000000000000000000000001002");
        inputAddress.add("0000000000000000000000000000000000000000000000000000000000001003");
        inputAddress.add("0000000000000000000000000000000000000000000000000000000000001004");
        inputAddress.add("0000000000000000000000000000000000000000000000000000000000001005");

        apiMsg = contract.newFunction("tick")
            .setParam(IBool.copyFrom(true))
            .setParam(IUint.copyFrom(inputUint))
            .setParam(IAddress.copyFrom(inputAddress))
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute();

        ContractResponse contractResponse = apiMsg.getObject();
        assertNotNull(contractResponse);

        System.out.println("Hash Response: " + contractResponse.getTxHash().toString());
        api.destroyApi();
    }

    @Test
    public void TestContractTicker() {
        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());
        List accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        Address acc = (Address) accs.get(0);
        if (!isEnoughBalance(acc)) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        apiMsg = api.getWallet().unlockAccount(acc, pw, 300);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        String ticker = readFile("ticker3.sol");

        apiMsg = api.getContractController()
            .createFromSource(ticker, acc, NRG_LIMIT_CONTRACT_CREATE_MAX, NRG_PRICE_MIN);
        assertFalse(apiMsg.isError());

        IContract contract = api.getContractController().getContract();
        assertNotNull(contract);

        apiMsg = api.getChain().blockNumber();
        assertFalse(apiMsg.isError());
        long blkNumber = apiMsg.getObject();

        ContractResponse cr;
        long cnt = 100;
        for (int i = 0; i < cnt; i++) {
            apiMsg = contract.newFunction("tick")
                .setFrom(acc)
                .setTxNrgLimit(NRG_LIMIT_TX_MAX)
                .setTxNrgPrice(NRG_PRICE_MIN)
                .build()
                .nonBlock()
                .execute();
            assertFalse(apiMsg.isError());
        }

        // assume the transaction should get within next 2 blocks
        while (true) {
            apiMsg = api.getChain().blockNumber();
            assertFalse(apiMsg.isError());
            long bestBlock = apiMsg.getObject();

            if (bestBlock > blkNumber + 1) {
                break;
            }
        }

        apiMsg = contract.newFunction("val")
            .setFrom(acc)
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute();
        assertFalse(apiMsg.isError());

        cr = apiMsg.getObject();
        assertNotNull(cr);
        assertNotNull(cr.getData());

        cnt = (Long) cr.getData().get(0);

        assertEquals(100L, cnt);

        api.destroyApi();
    }

    @Test
    public void TestContractThrow() {
        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());
        List accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        Address acc = (Address) accs.get(0);
        if (!isEnoughBalance(acc)) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        apiMsg = api.getWallet().unlockAccount(acc, pw, 300);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        String throwMe = readFile("throw.sol");

        apiMsg = api.getContractController()
            .createFromSource(throwMe, acc, NRG_LIMIT_CONTRACT_CREATE_MAX, NRG_PRICE_MIN);
        assertFalse(apiMsg.isError());

        IContract contract = api.getContractController().getContract();
        assertNotNull(contract);

        apiMsg = contract.newFunction("throwMe")
            .setFrom(acc)
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute();
        assertFalse(apiMsg.isError());

        ContractResponse cr = apiMsg.getObject();
        assertNotNull(cr);

        assertTrue(cr.getError().contentEquals("REVERT"));

        api.destroyApi();
    }

    @Test
    public void TestCustomerPayee2() {
        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());
        List accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        Address acc = (Address) accs.get(0);
        if (!isEnoughBalance(acc)) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        apiMsg = api.getWallet().unlockAccount(acc, pw, 300);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());

        String payee = readFile("customerPayee.sol");

        apiMsg = api.getContractController()
            .createFromSource(payee, acc, NRG_LIMIT_CONTRACT_CREATE_MAX, NRG_PRICE_MIN);
        assertFalse(apiMsg.isError());

        IContract contract = api.getContractController().getContract();
        assertNotNull(contract);

        assertThat(contract.getFrom(), is(equalTo(acc)));
        assertThat(contract.getContractAddress(), not(equalTo(null)));
        assertThat(contract.getAbiDefinition().size(), is(equalTo(14)));

        api.destroyApi();
    }

    @Test
    public void TestCustomerPayee() {
        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());
        List accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        Address acc = (Address) accs.get(0);
        if (!isEnoughBalance(acc)) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        apiMsg = api.getWallet().unlockAccount(acc, pw, 300);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());
        System.out.println("Account unlocked!");
        System.out.println();

        String payee = readFile("customerPayee.sol");

        ArrayList<ISolidityArg> param = new ArrayList<>();
        param.add(
            IAddress.copyFrom("1111111111111111111111111111111111111111111111111111111111111111"));
        param.add(
            IAddress.copyFrom("1111111111111111111111111111111111111111111111111111111111111111"));
        param.add(IUint.copyFrom(10));
        param.add(IUint.copyFrom(10));

        System.out.println("Prepare to deploy the token contract.");

        apiMsg = api.getContractController()
            .createFromSource(payee, acc, NRG_LIMIT_CONTRACT_CREATE_MAX, NRG_PRICE_MIN, param);
        assertFalse(apiMsg.isError());

        IContract ct = api.getContractController().getContract();
        assertNotNull(ct);

        System.out.println("Contract deployed!");
    }

    @Test
    public void tokenTest() {
        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());
        List accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.size() < 2) {
            System.out.println("this test need 2 accounts, skip this test!");
            return;
        }

        Address acc = (Address) accs.get(0);
        Address acc2 = (Address) accs.get(1);

        if (!isEnoughBalance(acc)) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        apiMsg = api.getWallet().unlockAccount(acc, pw, 300);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());
        System.out.println("Account unlocked!");
        System.out.println();

        String token = readFile("token.sol");

        ArrayList<ISolidityArg> param = new ArrayList<>();
        param.add(IUint.copyFrom(100000));
        param.add(ISString.copyFrom("Aion Token"));
        param.add(IUint.copyFrom(10));
        param.add(ISString.copyFrom("AION"));

        apiMsg = api.getContractController()
            .createFromSource(token, acc, NRG_LIMIT_CONTRACT_CREATE_MAX, NRG_PRICE_MIN, param);
        assertFalse(apiMsg.isError());

        IContract contract = api.getContractController().getContract();

        //Check initial default account balance
        apiMsg = contract.newFunction("balanceOf")
            .setFrom(acc)
            .setParam(IAddress.copyFrom(acc.toString()))
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute();
        assertFalse(apiMsg.isError());

        ContractResponse cr = apiMsg.getObject();
        assertNotNull(cr);
        assertNotNull(cr.getData());

        for (Object a : cr.getData()) {
            System.out.println("balanceOf " + acc.toString() + ": " + a.toString());
            assertThat(a.toString(), is(equalTo("100000")));
        }

        //Transfer balance to another account
        apiMsg = contract.newFunction("transfer")
            .setParam(IAddress.copyFrom(acc2.toString()))
            .setParam(IUint.copyFrom(1))
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute();
        assertFalse(apiMsg.isError());

        cr = apiMsg.getObject();
        assertNotNull(cr);

        //Check account2's balance
        apiMsg = contract.newFunction("balanceOf")
            .setParam(IAddress.copyFrom(acc2.toString()))
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute();
        assertFalse(apiMsg.isError());

        cr = apiMsg.getObject();
        assertNotNull(cr);
        assertNotNull(cr.getData());

        for (Object a : cr.getData()) {
            System.out.println("new balanceOf " + acc2.toString() + ": " + a.toString());
            assertThat(a.toString(), is(equalTo("1")));
        }

        //Check account1's balance
        apiMsg = contract.newFunction("balanceOf")
            .setParam(IAddress.copyFrom(acc.toString()))
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute();
        assertFalse(apiMsg.isError());

        cr = apiMsg.getObject();
        assertNotNull(cr);

        for (Object a : cr.getData()) {
            System.out.println("new balanceOf " + acc.toString() + ": " + a.toString());
            assertThat(a.toString(), is(equalTo("99999")));
        }

        api.destroyApi();
    }

    @Test
    public void testRevertOperation() {
        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());
        List accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        Address acc = (Address) accs.get(0);
        if (!isEnoughBalance(acc)) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        apiMsg = api.getWallet().unlockAccount(acc, pw, 300);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());
        System.out.println("Account unlocked!");
        System.out.println();
        /* deploy contract */
        String contract = readFile("revert.sol");

        apiMsg = api.getContractController()
            .createFromSource(contract, acc, NRG_LIMIT_CONTRACT_CREATE_MAX,
                NRG_PRICE_MIN);
        assertFalse(apiMsg.isError());

        IContract ct = api.getContractController().getContract();
        assertNotNull(ct);
        assertNotNull(ct.getContractAddress());
        System.out.println("Contract Address: " + ct.getContractAddress().toString());

        /* getData */
        apiMsg = ct.newFunction("getData")
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute();
        assertFalse(apiMsg.isError());

        ContractResponse rsp = apiMsg.getObject();
        assertNotNull(rsp);
        assertNotNull(rsp.getData());
        assertThat(rsp.getData().get(0), is(equalTo(3L)));

        /* setData and getData */
        apiMsg = ct.newFunction("setData")
            .setParam(IUint.copyFrom(5L))
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute();
        assertFalse(apiMsg.isError());

        rsp = apiMsg.getObject();
        assertNotNull(rsp);

        apiMsg = ct.newFunction("getData")
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute();
        assertFalse(apiMsg.isError());

        rsp = apiMsg.getObject();
        assertNotNull(rsp);
        assertNotNull(rsp.getData());

        assertThat(rsp.getData().get(0), is(equalTo(5L)));

        /* setData2 and getData */
        apiMsg = ct.newFunction("setData2")
            .setParam(IUint.copyFrom(7L))
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute();
        assertFalse(apiMsg.isError());

        rsp = apiMsg.getObject();
        assertNotNull(rsp);
        assertNotNull(rsp.getData());

        apiMsg = ct.newFunction("getData")
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute();
        assertFalse(apiMsg.isError());

        rsp = apiMsg.getObject();
        assertNotNull(rsp);
        assertNotNull(rsp.getData());

        assertThat(rsp.getData().get(0), is(equalTo(5L)));

        api.destroyApi();
    }

    @Test
    public void testTickerOperation() {
        connectAPI();

        ApiMsg apiMsg = api.getWallet().getAccounts();
        assertFalse(apiMsg.isError());
        List accs = apiMsg.getObject();
        assertNotNull(accs);

        if (accs.isEmpty()) {
            System.out.println("Empty account, skip this test!");
            return;
        }

        Address acc = (Address) accs.get(0);
        if (!isEnoughBalance(acc)) {
            System.out.println("balance of the account is not enough, skip this test!");
            return;
        }

        apiMsg = api.getWallet().unlockAccount(acc, pw, 300);
        assertFalse(apiMsg.isError());
        assertTrue(apiMsg.getObject());
        System.out.println("Account unlocked!");
        System.out.println();
        /* deploy contract */
        String contract = readFile("ticker4.sol");

        apiMsg = api.getContractController()
            .createFromSource(contract, acc, NRG_LIMIT_CONTRACT_CREATE_MAX,
                NRG_PRICE_MIN);
        assertFalse(apiMsg.isError());

        IContract ct = api.getContractController().getContract();
        assertNotNull(ct);
        assertNotNull(ct.getContractAddress());
        System.out.println("Contract Address: " + ct.getContractAddress().toString());

        /* getData */
        apiMsg = ct.newFunction("getData")
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute();
        assertFalse(apiMsg.isError());

        ContractResponse rsp = apiMsg.getObject();
        assertNotNull(rsp);
        assertNotNull(rsp.getData());
        assertThat(rsp.getData().get(0), is(equalTo(1L)));

        /* setData and getData */
        apiMsg = ct.newFunction("tick")
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute();
        assertFalse(apiMsg.isError());

        apiMsg = ct.newFunction("getData")
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute();
        assertFalse(apiMsg.isError());

        rsp = apiMsg.getObject();
        assertNotNull(rsp);
        assertNotNull(rsp.getData());
        assertThat(rsp.getData().get(0), is(equalTo(2L)));

        for (int i = 0; i < 2; i++) {
            apiMsg = ct.newFunction("tick")
                .setTxNrgLimit(NRG_LIMIT_TX_MAX)
                .setTxNrgPrice(NRG_PRICE_MIN)
                .build()
                .execute();
            assertFalse(apiMsg.isError());
        }

        apiMsg = ct.newFunction("getData")
            .setTxNrgLimit(NRG_LIMIT_TX_MAX)
            .setTxNrgPrice(NRG_PRICE_MIN)
            .build()
            .execute();
        assertFalse(apiMsg.isError());

        rsp = apiMsg.getObject();
        assertNotNull(rsp);
        assertNotNull(rsp.getData());
        assertThat(rsp.getData().get(0), is(equalTo(4L)));

        // need to register event to the kernel
//        List<ContractEvent> ce = ct.getEvents();
//        assertThat(ce.size(), is(equalTo(3)));

        //TODO: test ContractEventFilter
        //ContractEventFilter.ContractEventFilterBuilder builder = new ContractEventFilter.ContractEventFilterBuilder()
        //        .fromBlock("latest")
        //        .toBlock("10")
        //        .topics(new ArrayList())
        //        .expireTime(0)
        //        .addresses(new ArrayList());

        //msg = ct.queryEvents(builder.createContractEventFilter());
        //if (msg.isError()) {
        //    throw new Exception();
        //}
        //
        //List<ContractEvent> cts = msg.getObject();
        //assertThat(cts.size(), is(equalTo(4)));
        //
        //List<String> topics = new ArrayList();
        //topics.add("Ti");
        //
        //builder.topics(topics);
        //
        //msg = ct.queryEvents(builder.createContractEventFilter());
        //if (msg.isError()) {
        //    throw new Exception();
        //}
        //
        //cts = msg.getObject();
        //assertThat(cts.size(), is(equalTo(1)));

        api.destroyApi();
    }
}
