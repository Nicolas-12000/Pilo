import Link from "next/link";

export default function ProcedureNotFound() {
  return (
    <main className="flex flex-1 flex-col items-center justify-center px-4 py-16 text-center">
      <h1 className="font-headline-lg text-on-surface">Trámite no encontrado</h1>
      <p className="mt-3 max-w-md text-body-md text-on-surface-variant">
        Ese trámite no existe o ya no está disponible en el catálogo.
      </p>
      <Link
        href="/tramites"
        className="mt-8 inline-flex h-12 items-center rounded-md bg-primary px-5 font-label text-on-primary hover:bg-primary-hover"
      >
        Volver al catálogo
      </Link>
    </main>
  );
}
