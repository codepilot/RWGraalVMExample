import { com_xilinx_rapidwright_design_Design, GraalIsolate } from "./bun-classes-nogc";

using graalIsolate = new GraalIsolate();

function test_getDevice(devName: string): void {
  graalIsolate.loadDevice(devName);
  console.log(graalIsolate.getTileName(devName, 100, 100));
}

function test_readCheckpoint(dcpPathStr: string, edfPathStr: string): void {
  using dcpPath = graalIsolate.utf8ToJavaString(dcpPathStr);
  using edfPath = graalIsolate.utf8ToJavaString(edfPathStr);
  using design = com_xilinx_rapidwright_design_Design.readCheckpoint(graalIsolate.thread, dcpPath, edfPath);
  using cellCollection = design.getCells();
  using netCollection = design.getNets();
  using cells = cellCollection.createJAVA_ARRAY();
  using nets = netCollection.createJAVA_ARRAY();
  const cellStringToID = new Map<string, number>();
  const netStringToID = new Map<string, number>();

  for(const [i, cell] of cells.entries()) {
    cellStringToID.set(cell.toString(), i);
  }

  for(const [i, net] of nets.entries()) {
    netStringToID.set(net.toString(), i);
  }

  console.log(netStringToID);
}

switch(Bun.argv[2]) {
  case 'test_getDevice': {
    const devName =  String(Bun.argv[3]);
    test_getDevice(devName);
    break;
  }
  case 'test_readCheckpoint': {
    const dcpPath = String(Bun.argv[3]);
    const edfPath = String(Bun.argv[4]);
    test_readCheckpoint(dcpPath, edfPath);
    break;
  }
}
